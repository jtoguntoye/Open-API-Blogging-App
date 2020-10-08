package com.codingwithmitch.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.ui.main.MainActivity
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_auth.*
import timber.log.Timber

@AndroidEntryPoint
class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener{

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
       authViewModel.cancelActiveJobs()
    }

    val authViewModel: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        Timber.d("authViewmodel is ${authViewModel.hashCode()}")
        subscribeObservers()
        checkPreviousAuthUser()
    }

    override fun expandAppBar() {
        //ignore
    }

    private fun subscribeObservers() {

        authViewModel.dataState.observe(this, Observer {dataState ->
            onDataStateChange(dataState)
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
                    event.peekContent().let{ response ->
                        response.message?.let{ message ->
                            if(message.equals(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE)){
                                onFinishCheckPreviousAuthUser()
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

    fun checkPreviousAuthUser() {
        authViewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
    }

    private fun onFinishCheckPreviousAuthUser(){
        fragment_container.visibility = View.VISIBLE
    }

    fun navToMainActivity() {
        Timber.d("navMainActivity called")
        val intent = Intent(this, MainActivity::class.java)
       startActivity(intent)
        finish()
    }

    override fun displayProgressBar(boolean: Boolean?) {
        boolean?.let{
       if(boolean) {
           progress_bar.visibility = View.VISIBLE
       }
        else{
           progress_bar.visibility = View.GONE
       }
    }
    }
}