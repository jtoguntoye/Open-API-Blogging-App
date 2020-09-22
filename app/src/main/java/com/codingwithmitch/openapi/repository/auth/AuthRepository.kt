package com.codingwithmitch.openapi.repository.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_response.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_response.RegistrationResponse
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager
){
    private var repositoryJob: Job? = null



    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldError = LoginFields(email, password).isValidForLogin()
        if(!loginFieldError.equals(LoginFields.LoginError.none())) {
            return returnErrorResponse(loginFieldError, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            sessionManager.isConectedToTheInternet()
        ){
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Timber.d("Network response is ${response}")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
              return openApiAuthService.login(email,password)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()
    }

    fun attemptRegistration(email: String,
                            username: String,
                            password: String,
                            confirmPassword:String):LiveData<DataState<AuthViewState>> {

        val registrationFieldsError = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if(!registrationFieldsError.equals(RegistrationFields.RegistrationError.none())) {
            return returnErrorResponse(registrationFieldsError, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<RegistrationResponse, AuthViewState> (
            sessionManager.isConectedToTheInternet()
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Timber.d("HandleApiSuccessResponse : ${response}")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )

            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
            return openApiAuthService.register(email,username,password, confirmPassword)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()

    }






    private fun returnErrorResponse(errorMessage: String, responseType: ResponseType): LiveData<DataState<AuthViewState>> {
        Timber.d("returnErrorResponse: ${errorMessage}")
        return object: LiveData<DataState<AuthViewState>>() {
        override fun onActive() {
            super.onActive()
            value = DataState.error(
                Response(
                    errorMessage,
                    responseType
                )
            )
        }
    }
    }

    fun cancelActiveJobs() {
        Timber.d("AuthRepository: canceling ongoing jobs")
        repositoryJob?.cancel()
    }


}