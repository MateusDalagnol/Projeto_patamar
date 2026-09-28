package com.patamar.app.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.patamar.app.data.model.EventCategory
import com.patamar.app.databinding.BottomSheetMapFiltersBinding

// Sheet nomeada (não anônima, ver EventDetailBottomSheet) com o mesmo padrão
// visual de chip usado em ExploreFragment/FilterActivity — sem inventar um
// componente novo só pra essa tela.
class MapFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMapFiltersBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_KEY = "map_filters_request"
        const val RESULT_CATEGORIES = "result_categories"
        private const val ARG_SELECTED = "arg_selected"

        fun newInstance(selected: Set<EventCategory>) = MapFilterBottomSheet().apply {
            arguments = bundleOf(ARG_SELECTED to selected.map { it.name }.toTypedArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMapFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selected = requireArguments().getStringArray(ARG_SELECTED)
            ?.mapNotNull { name -> runCatching { EventCategory.valueOf(name) }.getOrNull() }
            ?.toSet() ?: emptySet()

        binding.chipGroupCategories.removeAllViews()
        EventCategory.values().forEach { category ->
            val chip = Chip(requireContext()).apply {
                text = category.label
                isCheckable = true
                isChecked = category in selected
            }
            binding.chipGroupCategories.addView(chip)
        }

        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnClear.setOnClickListener {
            for (i in 0 until binding.chipGroupCategories.childCount) {
                (binding.chipGroupCategories.getChildAt(i) as Chip).isChecked = false
            }
        }
        binding.btnApply.setOnClickListener {
            val chosen = (0 until binding.chipGroupCategories.childCount)
                .map { binding.chipGroupCategories.getChildAt(it) as Chip }
                .filter { it.isChecked }
                .map { EventCategory.values()[binding.chipGroupCategories.indexOfChild(it)].name }
                .toTypedArray()
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_CATEGORIES to chosen))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
