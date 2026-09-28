package com.patamar.app.ui.map

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.patamar.app.data.model.Event
import dagger.hilt.android.AndroidEntryPoint

// Detalhe em quadro no centro da tela, aberto pelos pins do Mapa.
@AndroidEntryPoint
class EventDetailDialog : DialogFragment() {

    private val viewModel: EventDetailViewModel by viewModels()

    companion object {
        fun newInstance(event: Event) = EventDetailDialog().apply {
            arguments = EventDetailContent.args(event, showMapButton = false)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setDimAmount(0.6f)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = EventDetailContent.bind(
        this, inflater, container, viewModel,
        onViewOnMap = {},
        dismiss = { dismiss() }
    ).root

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
