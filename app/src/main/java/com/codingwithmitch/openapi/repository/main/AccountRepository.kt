package com.codingwithmitch.openapi.repository.main

import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.session.SessionManager
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Job
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

    fun cancelActiveJobs() {
        Timber.d("AccountRepository: canceling ongoing jobs..")
        repositoryJob?.cancel()
    }
}