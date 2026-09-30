package com.patamar.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.patamar.app.BuildConfig
import com.patamar.app.R
import com.patamar.app.databinding.FragmentProfileBinding
import com.patamar.app.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvVersion.text = "Versão beta ${BuildConfig.VERSION_NAME}"
        binding.switchNotifications.isChecked = viewModel.areNotificationsEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }

        binding.tvChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Em breve 😄", Toast.LENGTH_SHORT).show() // BETA
        }
        binding.tvAbout.setOnClickListener {
            Toast.makeText(requireContext(), "Patamar — guia de eventos urbanos em Curitiba", Toast.LENGTH_SHORT).show()
        }
        binding.tvTerms.setOnClickListener {
            Toast.makeText(requireContext(), "Em breve 😄", Toast.LENGTH_SHORT).show() // BETA
        }
        binding.tvPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Em breve 😄", Toast.LENGTH_SHORT).show() // BETA
        }

        binding.btnLogout.setOnClickListener { logout() }
        binding.btnCreateAccountGuest.setOnClickListener {
            startActivity(AuthActivity.intent(requireContext(), startAtRegister = true))
        }
        binding.btnLoginGuest.setOnClickListener {
            startActivity(AuthActivity.intent(requireContext()))
        }

        if (viewModel.isGuest) {
            binding.groupGuest.visibility = View.VISIBLE
            binding.groupAccount.visibility = View.GONE
        } else {
            binding.groupGuest.visibility = View.GONE
            binding.groupAccount.visibility = View.VISIBLE
            observeUser()
        }
    }

    private fun observeUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                    if (user != null) {
                        binding.tvName.text = user.name
                        binding.tvEmail.text = user.email
                        binding.tvAvatarInitial.text = user.name.firstOrNull()?.uppercase() ?: "?"
                    }
                }
            }
        }
    }

    private fun logout() {
        viewModel.logout()
        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
