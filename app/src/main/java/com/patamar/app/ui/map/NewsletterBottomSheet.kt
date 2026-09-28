package com.patamar.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.patamar.app.data.local.mock.MockDataSource
import com.patamar.app.databinding.BottomSheetNewsletterBinding

// Resumo diário de eventos: exibida 1x por dia (persistido via EncryptedPrefsManager,
// ver MapFragment.scheduleNewsletter), na primeira vez que o app abre naquele dia.
class NewsletterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetNewsletterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetNewsletterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val highlighted = MockDataSource.events.filter { it.isHighlighted }.take(3)
        val first = highlighted.firstOrNull()

        if (first != null) {
            binding.tvEventName.text = first.name
            binding.tvEventInfo.text = "${first.address} · ${first.date}"
        }

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnViewEvent.setOnClickListener { dismiss() } // TODO: produção — abrir EventDetailBottomSheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "newsletter"
    }
}
