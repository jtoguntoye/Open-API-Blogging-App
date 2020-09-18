package com.codingwithmitch.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity(){

    @Inject
    lateinit var sessionManager: SessionManager

    val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        Timber.d("authViewmodel is ${authViewModel.hashCode()}")
        subscribeObservers()
    }

    private fun subscribeObservers() {
        authViewModel.dataState.observe(this, Observer {dataState ->
            dataState.data?. let{ data->
                data.data?.let{ event ->
                     event.getContentIfNotHandled()?.let{
                         it.authToken?.let{
                             Timber.d("AuthActivity, DataState:${it}")
                             authViewModel.setAuthToken(it)
                         }
                     }
                }

                data.response?.let{event ->
                    event.getContentIfNotHandled()?.let{
                        when(it.responseType) {
                            is ResponseType.Dialog-> {
                            //inflate dialog
                            }
                            is ResponseType.Toast -> {
                            //show Toast
                            }
                            is ResponseType.None -> {
                            Timber.d("AuthActivity, Response: ${it.message}")
                            }
                        }
                    }

                }
            }
        })

            authViewModel.viewState.observe(this, Observer{
                Timber.d("AuthActivity, subscribeObservers: AuthViewState: ${it}")
                it.authToken?.let{
                    sessionManager.login(it)
                }
            })

        sessionManager.cachedToken.observe(this, Observer {authToken->
            Timber.d("AuthActivity subscriber AuthTokenState: $authToken")
            if(authToken != null && authToken.account_pk != -1 && authToken.token != null) {
            navToMainActivity()

            }
        })

    }

    fun navToMainActivity() {
        Timber.d("navMainActivity called")
        val intent = Intent(this, MainActivity::class.java)
       startActivity(intent)
        finish()
    }
}