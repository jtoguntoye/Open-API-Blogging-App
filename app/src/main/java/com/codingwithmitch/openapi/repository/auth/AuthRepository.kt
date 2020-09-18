package com.codingwithmitch.openapi.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_response.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_response.RegistrationResponse
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val authApiService: OpenApiAuthService,
    val sessionManager: SessionManager
){

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        return authApiService.login(email, password)
            .switchMap {response->
                object: LiveData<DataState<AuthViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        when(response) {

                            is ApiSuccessResponse ->{
                                value = DataState.data(
                                    data = AuthViewState(
                                        authToken = AuthToken(
                                            response.body.pk,
                                            response.body.token
                                        )
                                    ),
                                    response = null
                                )
                            }

                            is ApiErrorResponse -> {
                                value = DataState.error(
                                    Response(
                                        message = response.errorMessage,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                            is ApiEmptyResponse -> {
                                value = DataState.error(
                                    Response(
                                        message = ERROR_UNKNOWN,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                        }

                    }
                }
            }
    }

    fun attemptRegistration(email: String,
                            userName:String,
                            password: String,
                            confirmPassword:String) : LiveData<DataState<AuthViewState>> {
        return authApiService.register(email, userName, password, confirmPassword).
                switchMap {response->
                    object :LiveData<DataState<AuthViewState>>(){
                        override fun onActive() {
                            super.onActive()

                            when(response) {
                                is ApiSuccessResponse -> {
                                    value = DataState.data(
                                        data = AuthViewState(
                                            authToken = AuthToken(response.body.pk, response.body.token)
                                        ),
                                        response = null
                                    )
                                }

                                is ApiErrorResponse -> {
                                   value = DataState.error(
                                       Response(
                                           message = response.errorMessage,
                                           responseType = ResponseType.Dialog()
                                       )
                                   )
                                }
                                is ApiEmptyResponse -> {
                                    value = DataState.error(
                                        Response(
                                            message = ERROR_UNKNOWN,
                                            responseType = ResponseType.Dialog()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
    }

}