package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login2.*
import timber.log.Timber


@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login2) {

    private val authViewModel: AuthViewModel by navGraphViewModels(R.id.auth_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Login fragment: ${authViewModel}")

        subscribeObservers()
    }

    fun subscribeObservers() {
        authViewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.loginFields?.let {loginFields->
                loginFields.login_email?. let{input_email.setText(it)}
                loginFields.login_password?.let{input_password.setText(it)}
            }
        } )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )

    }
}