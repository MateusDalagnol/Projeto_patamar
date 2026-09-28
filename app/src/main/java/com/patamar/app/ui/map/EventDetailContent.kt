package com.patamar.app.ui.map

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.google.android.material.snackbar.Snackbar
import com.patamar.app.R
import com.patamar.app.data.model.Event
import com.patamar.app.databinding.BottomSheetEventDetailBinding
import com.patamar.app.ui.auth.AuthActivity
import kotlinx.coroutines.launch

// Conteúdo compartilhado pelo detalhe em bottom sheet (Explorar/Salvos) e pelo
// detalhe em quadro central (Mapa): mesma tela, só muda o "recipiente".
object EventDetailContent {
    const val ARG_ID = "arg_id"
    private const val ARG_NAME = "arg_name"
    private const val ARG_DESCRIPTION = "arg_description"
    private const val ARG_ADDRESS = "arg_address"
    private const val ARG_DATE_TIME = "arg_date_time"
    private const val ARG_CATEGORY_LABEL = "arg_category_label"
    private const val ARG_CATEGORY_COLOR = "arg_category_color"
    private const val ARG_IS_FREE = "arg_is_free"
    private const val ARG_IMAGE = "arg_image"
    private const val ARG_SHOW_MAP_BUTTON = "arg_show_map_button"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))

    fun args(event: Event, showMapButton: Boolean) = Bundle().apply {
        putString(ARG_ID, event.id)
        putString(ARG_NAME, event.name)
        putString(ARG_DESCRIPTION, event.description)
        putString(ARG_ADDRESS, event.address)
        putString(ARG_DATE_TIME, "${DATE_FORMAT.format(event.date)} · ${event.time}")
        putString(ARG_CATEGORY_LABEL, event.category.label)
        putString(ARG_CATEGORY_COLOR, event.category.colorHex)
        putBoolean(ARG_IS_FREE, event.isFree)
        putString(ARG_IMAGE, event.imageUrl)
        putBoolean(ARG_SHOW_MAP_BUTTON, showMapButton)
    }

    fun bind(
        fragment: Fragment,
        inflater: LayoutInflater,
        container: ViewGroup?,
        viewModel: EventDetailViewModel,
        onViewOnMap: (String) -> Unit,
        dismiss: () -> Unit
    ): BottomSheetEventDetailBinding {
        val binding = BottomSheetEventDetailBinding.inflate(inflater, container, false)
        val args = fragment.requireArguments()
        val id = args.getString(ARG_ID).orEmpty()

        val color = try {
            Color.parseColor(args.getString(ARG_CATEGORY_COLOR))
        } catch (e: Exception) {
            Color.parseColor("#3F3F46")
        }
        binding.ivCover.load(args.getString(ARG_IMAGE)) {
            crossfade(true)
            placeholder(ColorDrawable(color))
            error(ColorDrawable(color))
        }
        binding.tvEventCategory.text = args.getString(ARG_CATEGORY_LABEL)
        binding.tvEventName.text = args.getString(ARG_NAME)
        binding.tvEventDescription.text = args.getString(ARG_DESCRIPTION)
        binding.tvEventAddress.text = args.getString(ARG_ADDRESS)
        binding.tvEventDateTime.text = args.getString(ARG_DATE_TIME)
        binding.tvEventFree.text = if (args.getBoolean(ARG_IS_FREE)) "Gratuita" else "Paga"

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnViewOnMap.visibility = if (args.getBoolean(ARG_SHOW_MAP_BUTTON)) View.VISIBLE else View.GONE
        binding.btnViewOnMap.setOnClickListener {
            onViewOnMap(id)
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            if (viewModel.canSave) {
                viewModel.toggleSaved()
            } else {
                Snackbar.make(binding.root, R.string.event_save_needs_account, Snackbar.LENGTH_LONG)
                    .setAction("Entrar") {
                        fragment.startActivity(AuthActivity.intent(fragment.requireContext()))
                    }.show()
            }
        }
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isSaved.collect { saved ->
                    val ctx = binding.root.context
                    binding.btnSave.setText(if (saved) R.string.event_detail_saved else R.string.event_detail_save)
                    binding.btnSave.setIconResource(if (saved) R.drawable.ic_star else R.drawable.ic_star_outline)
                    binding.btnSave.iconTint = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, if (saved) R.color.star_saved else R.color.text_primary)
                    )
                }
            }
        }
        return binding
    }
}
