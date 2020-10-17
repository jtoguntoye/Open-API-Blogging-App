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
import com.codingwithmitch.openapi.repository.JobManager
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
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
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
): JobManager("AuthRepository"){


    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldError = LoginFields(email, password).isValidForLogin()
        if(!loginFieldError.equals(LoginFields.LoginError.none())) {
            return returnErrorResponse(loginFieldError, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse, Any,AuthViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            true,
            false
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
                    AuthToken(
                        response.body.pk,
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
                addJob("attemptLogin", job)
            }
            //not used in this case
            override suspend fun createCacheRequestAndReturn() {
                //Not used in this case i.e networkRequest case
            }
            //not used in this case
            override fun loadFromCache(): LiveData<AuthViewState> {
                TODO("Not yet implemented")
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                TODO("Not yet implemented")
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
        return object : NetworkBoundResource<RegistrationResponse,Any, AuthViewState> (
            sessionManager.isConectedToTheInternet(),
             true,
             true,
           false

        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Timber.d("HandleApiSuccessResponse : ${response}")

                if(response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                //insert into AccountProperties table so you can insert into auth_token b/c of foreign key relationship
                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(response.body.pk,
                        response.body.email,
                        response.body.username)
                )

                // will return -1 if failure
                if(result1 < 0){
                    onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_ACCOUNT_PROPERTIES, ResponseType.Dialog()))
                    )
                    return
                }

                // will return -1 if failure
                val result2 = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result2 < 0) {
                     onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN,
                                ResponseType.Dialog())
                        )
                    )
                    return
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
               addJob("attemptRegistration", job)
            }
            //Not used in this case i.e u need network to register
            override suspend fun createCacheRequestAndReturn() {

            }
            //not used in this case
            override fun loadFromCache(): LiveData<AuthViewState> {
                TODO("Not yet implemented")
            }

            //not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
                TODO("Not yet implemented")
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
        return object : NetworkBoundResource<Void,Any, AuthViewState> (
            sessionManager.isConectedToTheInternet(),
            false,
            false,
           false

        ) {
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Timber.d("previousAuthUSer: Searching for token : $accountProperties")
                    accountProperties?.let {
                        if (accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if (authToken != null) {
                                    if (authToken.token != null) {
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
             addJob("checkPreviousAuthUser", job)
                }

            //not used in this case
            override fun loadFromCache(): LiveData<AuthViewState> {
                TODO("Not yet implemented")
            }

            //not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
                TODO("Not yet implemented")
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
        sharePrefsEditor.apply()
        val value: String? = sharedPreferences.getString(PreferencesKey.PREVIOUS_AUTH_USER, null)
        Timber.d("savedEmail = $value")

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

}