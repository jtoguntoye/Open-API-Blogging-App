package com.codingwithmitch.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.api.main.responses.BlogListSearchResponse
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.repository.JobManager
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.util.Constants.Companion.PAGINATION_PAGE_SIZE
import com.codingwithmitch.openapi.util.DateUtils
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@ActivityRetainedScoped
class BlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository") {

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        page: Int
    ):LiveData<DataState<BlogViewState>>{
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            false,
            true
        ){
            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main){
                    result.addSource(loadFromCache()){blogViewState->
                        blogViewState.blogFields.isQueryInProgress = false
                        if(page * PAGINATION_PAGE_SIZE > blogViewState.blogFields.blogList.size) {
                            blogViewState.blogFields.isQueryExhausted = true

                        }
                        onCompleteJob( DataState.data( blogViewState, null)
                        )
                    }
                }

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for(blogPostResponse in response.body.results){
                    blogPostList.add(
                        BlogPost(
                            pk = blogPostResponse.pk,
                            title = blogPostResponse.title,
                            slug = blogPostResponse.slug,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            date_updated = DateUtils.convertServerStringDateToLong(
                                blogPostResponse.date_updated),
                            username = blogPostResponse.username
                        )
                    )
                }

                updateLocalDb(blogPostList)
                createCacheRequestAndReturn()

            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return openApiMainService.searchListBlogPosts("Token ${authToken.token!!}",
                    query = query,
                page = page)
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
              return blogPostDao.getAllBlogPosts(
                  query = query,
                  page = page
              ).switchMap {BlogList->
                 object : LiveData<BlogViewState>(){
                     override fun onActive() {
                         super.onActive()
                         value = BlogViewState(
                             BlogViewState.BlogFields(
                                 blogList = BlogList,
                                 isQueryInProgress =  true
                             )
                         )
                     }
                 }

              }
            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                if(cacheObject != null) {
                    withContext(IO){
                        for(blogPost in cacheObject){
                            try{
                                // Launch each insert as a separate job to be executed in parallel
                               val j = launch {
                                    Timber.d("UpdateLocalDb: inserting blogPost: ${blogPost}")
                                    blogPostDao.insert(blogPost)

                               }
                                j.join() // wait for completion before proceeding to next
                            }

                            catch (e:Exception){
                                Timber.d("UpdateLocalDb: error updating cache data on blog post: ${blogPost.slug}"+ e.message)
                            }

                        }
                    }
                }
                else{
                    "updateLocaldb: blog post list is null"
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts",job = job)
            }
        }.asLiveData()

    }

}