package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_launcher1.*
import kotlinx.android.synthetic.main.fragment_register2.*
import timber.log.Timber

@AndroidEntryPoint
class LauncherFragment : Fragment(R.layout.fragment_launcher1) {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        register.setOnClickListener {
        navRegistration()
        }
        login.setOnClickListener{
        navLogin()
        }
        forgot_password.setOnClickListener {
            navForgotPassword()

        }
        focusable_view.requestFocus()
        Timber.d("Launcher fragment: ${authViewModel}")
    }

    private fun navForgotPassword() {
        findNavController().navigate(R.id.action_launcherFragment_to_forgotPasswordFragment)
    }

    private fun navLogin() {
        findNavController().navigate(R.id.action_launcherFragment_to_loginFragment)
    }

    private fun navRegistration() {
        findNavController().navigate(R.id.action_launcherFragment_to_registerFragment)
    }
}