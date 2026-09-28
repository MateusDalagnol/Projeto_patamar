package com.patamar.app.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.patamar.app.R
import com.patamar.app.core.security.EncryptedPrefsManager
import com.patamar.app.core.utils.Constants
import com.patamar.app.data.model.Event
import com.patamar.app.data.model.EventCategory
import com.patamar.app.databinding.FragmentMapBinding
import com.patamar.app.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import java.io.File

// OPÇÃO A ATIVA — OSMDroid (raster tiles, sem API key).
// Para trocar para MapLibre (Opção B), veja MapFragmentMapLibre.kt.reference
// no mesmo pacote e o comentário em app/build.gradle.kts.
//
// PATCH v0.1.2: o CartoDB Dark Matter (dark_all) usado no patch v0.1.1 passou
// a exigir API key da CARTO — sem key, o servidor devolve o tile real com um
// watermark "API KEY REQUIRED" por cima. Como o app não usa nenhuma chave paga
// em lugar nenhum (design intencional do beta), voltamos para o tile padrão
// do OpenStreetMap (Mapnik), que é o único raster genuinamente gratuito e sem
// cadastro que o OSMDroid oferece pronto pra uso.

@AndroidEntryPoint
class MapFragment : Fragment() {

    @Inject lateinit var prefsManager: EncryptedPrefsManager

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var myLocationProvider: GpsMyLocationProvider? = null
    private var myLocationMarker: Marker? = null
    private var hasCenteredOnLocation = false

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableLocationTracking()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Configuração obrigatória do OSMDroid antes de inflar o MapView
        Configuration.getInstance().apply {
            userAgentValue = "Patamar/0.1-beta (opensource)"
            osmdroidBasePath = File(requireContext().cacheDir, "osmdroid")
            osmdroidTileCache = File(requireContext().cacheDir, "osmdroid/tiles")
        }
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK) // OSM padrão — sem API key
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            minZoomLevel = Constants.MIN_MAP_ZOOM
            maxZoomLevel = Constants.MAX_MAP_ZOOM
            controller.setZoom(Constants.DEFAULT_MAP_ZOOM)
            controller.setCenter(GeoPoint(Constants.CURITIBA_LAT, Constants.CURITIBA_LNG))
        }

        // Atribuição obrigatória do OpenStreetMap
        binding.tvOsmAttribution.text = "© OpenStreetMap contributors"

        binding.btnFilters.setOnClickListener { showQuickFilterSheet() }

        childFragmentManager.setFragmentResultListener(
            MapFilterBottomSheet.REQUEST_KEY, viewLifecycleOwner
        ) { _, result ->
            val selected = result.getStringArray(MapFilterBottomSheet.RESULT_CATEGORIES)
                ?.mapNotNull { name -> runCatching { EventCategory.valueOf(name) }.getOrNull() }
                ?.toSet() ?: emptySet()
            viewModel.applyQuickFilter(selected)
        }

        requestLocationPermission()
        observeEvents()
        observeQuickFilter()
        scheduleNewsletter()
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.visibleEvents.collect { events ->
                    addEventMarkers(events)
                    binding.tvEventCount.text = "${events.size} eventos próximos"
                    handlePendingFocusEvent(events)
                }
            }
        }
    }

    // "Ver no mapa" vindo do Explorar (ou de outro marcador): centraliza e
    // reabre o detalhe. Se um filtro estiver escondendo o evento pedido,
    // limpa o filtro em vez de simplesmente falhar silenciosamente.
    private fun handlePendingFocusEvent(events: List<Event>) {
        val id = mainViewModel.pendingFocusEventId.value ?: return
        val event = events.firstOrNull { it.id == id }
        if (event == null) {
            if (viewModel.quickFilterCategories.value.isNotEmpty()) {
                viewModel.applyQuickFilter(emptySet())
            }
            return
        }
        // Foco explícito num evento vale mais que centrar em você quando o 1º fix do GPS chegar.
        hasCenteredOnLocation = true
        binding.mapView.controller.animateTo(GeoPoint(event.lat, event.lng))
        showEventBottomSheet(event)
        mainViewModel.consumeFocusEvent()
    }

    private fun addEventMarkers(events: List<Event>) {
        val map = _binding?.mapView ?: return
        map.overlays.removeAll { it is Marker && it !== myLocationMarker }
        events.forEach { event ->
            val marker = Marker(map).apply {
                position = GeoPoint(event.lat, event.lng)
                title = event.name
                icon = EventMarkerHelper.createMarkerDrawable(requireContext(), event.category)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // ponta do pin = local exato
                setOnMarkerClickListener { _, _ ->
                    showEventBottomSheet(event)
                    true
                }
            }
            map.overlays.add(marker)
        }
        // Garante que o ponto de localização do usuário sempre fique por cima
        // dos pins de evento (evita ficar escondido quando um evento cai perto).
        myLocationMarker?.let { marker ->
            map.overlays.remove(marker)
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    private fun showEventBottomSheet(event: Event) {
        EventDetailDialog.newInstance(event).show(childFragmentManager, "event_detail")
    }

    private fun showQuickFilterSheet() {
        MapFilterBottomSheet.newInstance(viewModel.quickFilterCategories.value)
            .show(childFragmentManager, "map_filters")
    }

    private fun observeQuickFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.quickFilterCategories.collect { selected ->
                    binding.btnFilters.text = if (selected.isEmpty()) {
                        getString(R.string.map_filters)
                    } else {
                        getString(R.string.map_filters_active, selected.size)
                    }
                }
            }
        }
    }

    private fun scheduleNewsletter() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && prefsManager.shouldShowDailyNewsletter()) {
                prefsManager.markNewsletterShownToday()
                NewsletterBottomSheet().show(childFragmentManager, NewsletterBottomSheet.TAG)
            }
        }, Constants.NEWSLETTER_DELAY_MS)
    }

    private fun requestLocationPermission() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        when {
            fineLocationGranted -> enableLocationTracking()
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Localização necessária")
                    .setMessage("O Patamar usa sua localização para mostrar eventos perto de você.")
                    .setPositiveButton("Permitir") { _, _ ->
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    .setNegativeButton("Agora não", null)
                    .show()
            }
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // NOTA: MyLocationNewOverlay (a classe "oficial" do OSMDroid pra isso) fica
    // habilitada e recebendo fixes normalmente, mas seu draw() nunca pinta nada
    // nesse setup — sem stacktrace, sem exceção, só não aparece. Em vez de
    // depender do overlay pronto, usamos um Marker comum (mesma classe dos pins
    // de evento, que sabemos que renderiza) e atualizamos a posição manualmente
    // a cada fix do GpsMyLocationProvider.
    private fun enableLocationTracking() {
        if (myLocationProvider != null) return

        val provider = GpsMyLocationProvider(requireContext())
        myLocationProvider = provider
        provider.startLocationProvider { location, _ ->
            if (location == null) return@startLocationProvider
            Handler(Looper.getMainLooper()).post {
                updateMyLocationMarker(location.latitude, location.longitude)
            }
        }
        // O provider só avisa em fixes NOVOS; com o GPS frio (ou o aparelho parado)
        // isso demora ou nunca chega. Usa a última posição conhecida já na abertura.
        provider.lastKnownLocation?.let { updateMyLocationMarker(it.latitude, it.longitude) }
    }

    private fun updateMyLocationMarker(lat: Double, lng: Double) {
        val map = _binding?.mapView ?: return
        val point = GeoPoint(lat, lng)

        val marker = myLocationMarker ?: Marker(map).apply {
            icon = BitmapDrawable(resources, createMyLocationDot())
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Você está aqui"
            myLocationMarker = this
            map.overlays.add(this)
        }
        marker.position = point

        if (!hasCenteredOnLocation) {
            hasCenteredOnLocation = true
            // Se veio de "Ver no mapa", o foco no evento tem prioridade sobre centrar em você.
            if (mainViewModel.pendingFocusEventId.value == null) map.controller.animateTo(point)
        }
        map.invalidate()
    }

    // Azul não é usado em nenhuma categoria de evento — fica reservado só pra
    // "você está aqui", seguindo a convenção do Google/Apple Maps.
    private fun createMyLocationDot(): Bitmap {
        val density = resources.displayMetrics.density
        val sizePx = (36f * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = sizePx / 2f

        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#332563EB") }
        canvas.drawCircle(center, center, center, haloPaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(center, center, 9f * density, ringPaint)

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2563EB") }
        canvas.drawCircle(center, center, 7f * density, dotPaint)

        return bitmap
    }

    override fun onResume() {
        super.onResume()
        _binding?.mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        _binding?.mapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myLocationProvider?.stopLocationProvider()
        myLocationProvider = null
        myLocationMarker = null
        _binding = null
    }
}
