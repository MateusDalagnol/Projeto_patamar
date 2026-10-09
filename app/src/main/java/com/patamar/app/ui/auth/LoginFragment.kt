package com.patamar.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.patamar.app.R
import com.patamar.app.core.utils.UiState
import com.patamar.app.databinding.FragmentLoginBinding
import com.patamar.app.ui.filter.FilterActivity
import com.patamar.app.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { submit() }
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                true
            } else false
        }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }

        binding.tvCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Recuperação de senha em breve", Toast.LENGTH_SHORT).show() // BETA
        }

        binding.tvContinueGuest.setOnClickListener {
            viewModel.continueAsGuest()
            goToMain()
        }

        observeState()
    }

    private fun submit() {
        binding.root.clearFocus()
        viewModel.login(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString()
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val loading = state is UiState.Loading
                        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                        binding.btnLogin.isEnabled = !loading
                        when (state) {
                            is UiState.Success -> goToFilter()
                            is UiState.Error -> binding.tilPassword.error = state.message
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.formState.collect { form ->
                        if (form.emailError != null) binding.tilEmail.error = form.emailError
                        if (form.passwordError != null) binding.tilPassword.error = form.passwordError
                    }
                }
            }
        }
    }

    // Após login, o fluxo passa por FilterActivity (personalização)
    private fun goToFilter() {
        startActivity(Intent(requireContext(), FilterActivity::class.java))
        requireActivity().finish()
    }

    private fun goToMain() {
        startActivity(
            Intent(requireContext(), MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
