package com.codingwithmitch.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
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
                  accountPropertiesDao.updateAccountproperties(
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
    fun cancelActiveJobs() {
        Timber.d("AccountRepository: canceling ongoing jobs..")
        repositoryJob?.cancel()
    }
}