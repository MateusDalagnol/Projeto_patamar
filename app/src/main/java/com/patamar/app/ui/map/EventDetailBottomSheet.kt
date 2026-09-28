package com.patamar.app.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.patamar.app.data.model.Event
import com.patamar.app.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

// Classe nomeada (não anônima) — Fragment exige isso pra poder ser recriado a
// partir do estado salvo. Usado no Explorar e nos Salvos; o Mapa usa EventDetailDialog.
@AndroidEntryPoint
class EventDetailBottomSheet : BottomSheetDialogFragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: EventDetailViewModel by viewModels()

    companion object {
        fun newInstance(event: Event) = EventDetailBottomSheet().apply {
            arguments = EventDetailContent.args(event, showMapButton = true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = EventDetailContent.bind(
        this, inflater, container, viewModel,
        onViewOnMap = { mainViewModel.requestFocusEvent(it) },
        dismiss = { dismiss() }
    ).root

    override fun onStart() {
        super.onStart()
        // O cartão do layout já traz cantos e borda; o fundo padrão do sheet some.
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundColor(Color.TRANSPARENT)
    }
}
