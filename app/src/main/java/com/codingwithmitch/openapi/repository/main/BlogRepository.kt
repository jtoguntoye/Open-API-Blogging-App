package com.codingwithmitch.openapi.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.GenericResponse
import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.api.main.responses.BlogCreateUpdateResponse
import com.codingwithmitch.openapi.api.main.responses.BlogListSearchResponse
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogPostDao
import com.codingwithmitch.openapi.persistence.returnOrderedBlogQuery
import com.codingwithmitch.openapi.repository.JobManager
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState.ViewBlogFields
import com.codingwithmitch.openapi.util.AbsentLiveData
import com.codingwithmitch.openapi.util.Constants.Companion.PAGINATION_PAGE_SIZE
import com.codingwithmitch.openapi.util.DateUtils
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.lang.Exception
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
        filterAndOrder: String,
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
                    ordering = filterAndOrder,
                page = page)
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
              return blogPostDao.returnOrderedBlogQuery(
                  query = query,
                  filterAndOrder = filterAndOrder,
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


    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return  object : NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            true,
            false
        ){
            //not applicable
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
               withContext(Dispatchers.Main) {
                   Timber.d("HandleApiSuccessResponse: ${response.body.response}")

                   if(response.body.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT)){
                       onCompleteJob(
                           DataState.data(
                               data = BlogViewState(
                                   viewBlogFields = ViewBlogFields(
                                       isAuthorOfBlogPost = false
                                   )
                               ),
                               response = null
                           )
                       )
                   }
                   else

                       if(response.body.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT)){
                           onCompleteJob(
                               DataState.data(
                                   data = BlogViewState(
                                       viewBlogFields = ViewBlogFields(
                                           isAuthorOfBlogPost = true
                                       )
                                   ),
                                   response = null
                               )
                           )
                       }
                   else {
                           onErrorReturn(ERROR_UNKNOWN, shouldUseDialog = false, shouldUseToast = false )
                       }
               }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.isAuthorOfBlogPost(
                    "Token ${authToken.token!!}",
                    slug
                )
            }

            //not applicable
            override fun loadFromCache(): LiveData<BlogViewState> {
               return AbsentLiveData.create()
            }

            //not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }


        }.asLiveData()

    }

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ):LiveData<DataState<BlogViewState>>{
        return object : NetworkBoundResource<GenericResponse, BlogPost,
                BlogViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            true,
            false
        ) {

            //not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                if(response.body.response == SUCCESS_BLOG_DELETED) {
                    updateLocalDb(blogPost)
                }
                else {
                    onCompleteJob(
                        DataState.error(
                            Response(ERROR_UNKNOWN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
               return openApiMainService.deleteBlogPost(
                   "Token ${authToken.token!!}",
                   blogPost.slug
               )
            }


            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.deleteBlogPost(blogPost)
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(SUCCESS_BLOG_DELETED,
                            ResponseType.Toast())
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob("deleteBlogPost", job)
            }
        }.asLiveData()
    }


    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<BlogViewState>>{
        return object: NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
            sessionManager.isConectedToTheInternet(),
            true,
            true,
            false
        ){
            //not applicable
            override suspend fun createCacheRequestAndReturn() {
            }


            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {

                val updatedBlogPost =  BlogPost(
                    response.body.pk,
                    response.body.title,
                    response.body.slug,
                    response.body.body,
                    response.body.image,
                    DateUtils.convertServerStringDateToLong(response.body.date_updated),
                    response.body.username
                )

                updateLocalDb(updatedBlogPost)

                withContext(Dispatchers.Main) {
                    //finish with success response
                    onCompleteJob(
                        DataState.data(
                            BlogViewState(
                                viewBlogFields = ViewBlogFields(
                                    blogPost = updatedBlogPost

                                )
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
               return openApiMainService.updateBlogPost(
                    "Token ${authToken.token!!}",
                   slug,
                   title,
                   body,
                   image
                )
            }

            //not used for this call
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
              cacheObject?.let{blogPost->
                  blogPostDao.updateBlogPost(
                      blogPost.pk,
                      blogPost.title,
                      blogPost.body,
                      blogPost.image
                  )
              }
            }

            override fun setJob(job: Job) {
                addJob("updateBlogPost", job)
            }
        }.asLiveData()

    }


}