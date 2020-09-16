package com.codingwithmitch.openapi.ui.auth

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent.*
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import com.codingwithmitch.openapi.util.AbsentLiveData

class AuthViewModel
@ViewModelInject
constructor(
    val authRepository: AuthRepository
): BaseViewModel<AuthStateEvent, AuthViewState>(){


    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if(update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields =  registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if(update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val updatedViewState = getCurrentViewStateOrNew()
        if(updatedViewState.authToken == authToken) {
            return
        }
        updatedViewState.authToken = authToken
        _viewState.value = updatedViewState
    }

    override fun initViewState(): AuthViewState {
       return AuthViewState()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {

        when (stateEvent) {
            is LoginAttemptEvent ->{
                return AbsentLiveData.create()
            }

            is RegisterAttemptEvent -> {
                return AbsentLiveData.create()
            }
            is CheckPreviousAuthEvent -> {
                return AbsentLiveData.create()
            }
        }
    }
}