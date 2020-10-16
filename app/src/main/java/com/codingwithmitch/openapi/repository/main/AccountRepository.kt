package com.codingwithmitch.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.GenericResponse
import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import com.codingwithmitch.openapi.util.AbsentLiveData
import com.codingwithmitch.openapi.util.GenericApiResponse
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@ActivityRetainedScoped
class AccountRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val sessionManager: SessionManager,
    val accountPropertiesDao: AccountPropertiesDao
    ) {

    private var repositoryJob: Job? = null

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            false,
            shouldLoadFromCache = true

        ) {
            override suspend fun createCacheRequestAndReturn() {
              withContext(Main){
                  //finish by viewing the db cache
                  result.addSource(loadFromCache()){accountViewState ->
                      onCompleteJob(DataState.data(
                          data = accountViewState ,
                          response =  null))

                  }
              }
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<AccountProperties>) {
              updateLocalDb(response.body)
              createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties(
                    "Token ${authToken.token}"
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                       object :LiveData<AccountViewState>(){
                           override fun onActive() {
                               super.onActive()
                               value = AccountViewState(it)
                           }
                       }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
              cacheObject?.let {accountProperties->
                  accountPropertiesDao.updateAccountProperties(
                      accountProperties.pk,
                      accountProperties.email,
                      accountProperties.username
                  )
              }
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }



        }.asLiveData()
    }


    fun saveAccountProperties(authToken: AuthToken, accountProperties: AccountProperties): LiveData<DataState<AccountViewState>> {

        return object: NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            true,
            false
        ){
            override suspend fun createCacheRequestAndReturn() {
                //not used in this case
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)

                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )

            }

            //not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()


    }


    fun updatePassword(authToken: AuthToken,
                      currentPassword: String,
                       newPassword: String,
                       confirmNewPassword: String):LiveData<DataState<AccountViewState>> {

        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            true,
            false
        ) {

            //not used in this case
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
            return  openApiMainService.updatePassword(
                 "Token ${authToken.token!!}",
                 currentPassword,
                 newPassword,
                 confirmNewPassword
             )
            }

            //not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            //not used in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }


            override fun setJob(job: Job) {
               repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()

    }
    fun cancelActiveJobs() {
        Timber.d("AccountRepository: canceling ongoing jobs..")
        repositoryJob?.cancel()
    }
}