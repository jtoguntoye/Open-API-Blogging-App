package com.codingwithmitch.openapi.repository.auth

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_response.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_response.RegistrationResponse
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.Data
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import com.codingwithmitch.openapi.util.*
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharePrefsEditor: SharedPreferences.Editor
){
    private var repositoryJob: Job? = null


    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldError = LoginFields(email, password).isValidForLogin()
        if(!loginFieldError.equals(LoginFields.LoginError.none())) {
            return returnErrorResponse(loginFieldError, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            sessionManager.isConectedToTheInternet(),
            isNetworkRequest = true
        ){
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Timber.d("Network response is ${response}")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }
                //insert into AccountProperties table so you can insert into auth_token b/c of foreign key relationship
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(response.body.pk,
                                    response.body.email,
                                    "")
                )
                //will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(response.body.pk,
                    response.body.token
                    )
                )
                if(result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN,
                                ResponseType.Dialog())
                        )
                    )
                }
                saveAuthenticatedUSerToPrefs(email)
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

            override suspend fun createCacheRequestAndReturn() {
                //Not used in this case i.e networkRequest case
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
            sessionManager.isConectedToTheInternet(),
            isNetworkRequest = true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Timber.d("HandleApiSuccessResponse : ${response}")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                //insert into AccountProperties table so you can insert into auth_token b/c of foreign key relationship
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(response.body.pk,
                        response.body.email,
                        "")
                )
                //will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(response.body.pk,
                        response.body.token
                    )
                )
                if(result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN,
                                ResponseType.Dialog())
                        )
                    )
                }
                saveAuthenticatedUSerToPrefs(email)

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
            //Not used in this case i.e u need network to register
            override suspend fun createCacheRequestAndReturn() {

            }
        }.asLiveData()

    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {

        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferencesKey.PREVIOUS_AUTH_USER, null)

        if(previousAuthUserEmail.isNullOrBlank()) {
            Timber.d("No previously authenticated user found...")
            return returnNoTokenFound()
        }
        else{
        return object : NetworkBoundResource<Void, AuthViewState> (
            sessionManager.isConectedToTheInternet(),
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Timber.d("previousAuthUSer: Searching for token : $accountProperties")
                    accountProperties?.let {
                        if (accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if (authToken != null) {
                                    onCompleteJob(
                                        DataState.data(
                                             AuthViewState(authToken = authToken)
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }
                    Timber.d("checkPreviousAuthToken: AuthToken not found..")
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                ResponseType.None()
                            )
                        )
                    )

                }

        }

            //Not used in this case
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
            }

            //not used in this case
            override fun createCall(): LiveData<GenericApiResponse<Void>> {
               return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
              repositoryJob?.cancel()
                repositoryJob = job
                }
            }.asLiveData()
        }
    }


    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return  object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())

                )
            }
        }

    }

    private fun saveAuthenticatedUSerToPrefs(email: String) {
        sharePrefsEditor.putString(PreferencesKey.PREVIOUS_AUTH_USER, email)
        val value: String? = sharedPreferences.getString(PreferencesKey.PREVIOUS_AUTH_USER, null)
        Timber.d("savedEmail = $value")
        sharePrefsEditor.apply()
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