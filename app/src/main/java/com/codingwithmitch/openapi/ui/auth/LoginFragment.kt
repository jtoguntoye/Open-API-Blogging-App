package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login2) {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Login fragment: ${authViewModel}")

        authViewModel.testLogin().observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is ApiSuccessResponse ->{
                    Timber.d( "LOGIN RESPONSE: ${response.body}")
                }
                is ApiErrorResponse ->{
                    Timber.d("LOGIN RESPONSE: ${response.errorMessage}")
                }
                is ApiEmptyResponse ->{
                    Timber.d("LOGIN RESPONSE: Empty Response")
                }
            }

        })
    }
}