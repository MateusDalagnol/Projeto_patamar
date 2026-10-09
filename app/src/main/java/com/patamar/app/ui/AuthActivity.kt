package com.patamar.app.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.patamar.app.R
import com.patamar.app.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // O NavHostFragment do layout hospeda LoginFragment <-> RegisterFragment
        // via nav_graph.xml (auth_nav_graph)

        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_START_AT_REGISTER, false)) {
            val host = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            host.navController.navigate(R.id.action_login_to_register)
        }
    }

    companion object {
        private const val EXTRA_START_AT_REGISTER = "extra_start_at_register"

        // Ponto único pra abrir login/cadastro a partir de Perfil, Salvos, etc.
        // Sem CLEAR_TASK: "voltar" devolve o usuário à tela de onde ele veio.
        fun intent(context: Context, startAtRegister: Boolean = false): Intent =
            Intent(context, AuthActivity::class.java).putExtra(EXTRA_START_AT_REGISTER, startAtRegister)
    }
}
