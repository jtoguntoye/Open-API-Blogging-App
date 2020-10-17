package com.codingwithmitch.openapi.repository.main

import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.repository.JobManager
import com.codingwithmitch.openapi.session.SessionManager
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class BlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val BlogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository") {


}