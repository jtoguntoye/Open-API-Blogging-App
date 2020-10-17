package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_register2.*
import kotlinx.android.synthetic.main.fragment_register2.input_email
import kotlinx.android.synthetic.main.fragment_register2.input_password
import timber.log.Timber

@AndroidEntryPoint
class RegisterFragment : BaseAuthFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Register fragment: ${authViewModel}")

        register_button.setOnClickListener {
            register()
        }
        subscribeObservers()
}

    fun subscribeObservers() {
        authViewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.registrationFields?.let {registrationFields->
               registrationFields.registration_email?. let{input_email.setText(it)}
                registrationFields.registration_Username?.let{input_username.setText(it)}
                registrationFields.registration_password?.let{input_password.setText(it)}
                registrationFields.registration_confirm_password?.let{input_password_confirm.setText(it)}
            }
        } )
    }

    fun register() {
        authViewModel.setStateEvent(
            AuthStateEvent.RegisterAttemptEvent(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            ))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.setRegistrationFields(
            RegistrationFields(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )

    }
}