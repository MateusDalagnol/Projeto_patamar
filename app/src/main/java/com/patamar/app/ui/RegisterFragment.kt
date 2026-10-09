package com.patamar.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.patamar.app.R
import com.patamar.app.core.utils.UiState
import com.patamar.app.databinding.FragmentRegisterBinding
import com.patamar.app.ui.auth.RegisterViewModel.Field
import com.patamar.app.ui.filter.FilterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener { submit() }
        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.cbTerms.requestFocus()
                true
            } else false
        }

        binding.etName.doAfterTextChanged { viewModel.clearFieldError(Field.NAME) }
        binding.etEmail.doAfterTextChanged { viewModel.clearFieldError(Field.EMAIL) }
        binding.etPassword.doAfterTextChanged { viewModel.clearFieldError(Field.PASSWORD) }
        binding.etConfirmPassword.doAfterTextChanged { viewModel.clearFieldError(Field.CONFIRM_PASSWORD) }
        binding.cbTerms.setOnCheckedChangeListener { _, _ -> viewModel.clearFieldError(Field.TERMS) }

        binding.tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        observeState()
    }

    private fun submit() {
        binding.root.clearFocus()
        viewModel.register(
            name = binding.etName.text.toString(),
            email = binding.etEmail.text.toString(),
            password = binding.etPassword.text.toString(),
            confirmPassword = binding.etConfirmPassword.text.toString(),
            termsAccepted = binding.cbTerms.isChecked
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val loading = state is UiState.Loading
                        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                        binding.btnRegister.isEnabled = !loading
                        if (state is UiState.Success) goToFilter()
                    }
                }
                launch {
                    viewModel.formState.collect { form ->
                        binding.tilName.error = form.nameError
                        binding.tilEmail.error = form.emailError
                        binding.tilPassword.error = form.passwordError
                        binding.tilConfirmPassword.error = form.confirmPasswordError
                        binding.tvTermsError.text = form.termsError.orEmpty()
                        binding.tvTermsError.visibility = if (form.termsError != null) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun goToFilter() {
        startActivity(Intent(requireContext(), FilterActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
