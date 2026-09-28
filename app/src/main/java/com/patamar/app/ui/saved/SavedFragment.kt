package com.patamar.app.ui.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.patamar.app.R
import com.patamar.app.core.extensions.showSnackbar
import com.patamar.app.data.model.Event
import com.patamar.app.databinding.FragmentSavedBinding
import com.patamar.app.ui.auth.AuthActivity
import com.patamar.app.ui.explore.adapter.EventListAdapter
import com.patamar.app.ui.map.EventDetailBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedFragment : Fragment(R.layout.fragment_saved) {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SavedViewModel by viewModels()

    private lateinit var adapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isGuest) {
            binding.groupGuest.visibility = View.VISIBLE
            binding.groupSaved.visibility = View.GONE
            binding.btnCreateAccountGuest.setOnClickListener {
                startActivity(AuthActivity.intent(requireContext(), startAtRegister = true))
            }
            return
        }

        binding.groupGuest.visibility = View.GONE
        binding.groupSaved.visibility = View.VISIBLE

        adapter = EventListAdapter(onClick = ::showEventDetail)
        binding.rvSaved.layoutManager = LinearLayoutManager(context)
        binding.rvSaved.adapter = adapter
        setupSwipeToDelete()
        observeState()
    }

    private fun showEventDetail(event: Event) {
        EventDetailBottomSheet.newInstance(event).show(childFragmentManager, "event_detail")
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: androidx.recyclerview.widget.RecyclerView, vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val event = adapter.currentList[viewHolder.bindingAdapterPosition]
                viewModel.removeEvent(event)
                binding.root.showSnackbar("Removido dos salvos", "Desfazer") {
                    // BETA: undo simplificado — não re-salva automaticamente
                }
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.rvSaved)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedEvents.collect { events ->
                    adapter.submitList(events)
                    binding.tvSavedCount.text = "Salvos (${events.size})"
                    binding.emptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvSaved.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
