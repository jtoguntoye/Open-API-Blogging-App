package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.util.GenericApiResponse
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register2) {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Register fragment: ${authViewModel}")

        authViewModel.testRegister().observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is GenericApiResponse.ApiSuccessResponse ->{
                    Timber.d( "REGISTER RESPONSE: ${response.body}")
                }
                is GenericApiResponse.ApiErrorResponse ->{
                    Timber.d("REGISTER RESPONSE: ${response.errorMessage}")
                }
                is GenericApiResponse.ApiEmptyResponse ->{
                    Timber.d("REGISTER RESPONSE: Empty Response")
                }
            }

        })
    }
}