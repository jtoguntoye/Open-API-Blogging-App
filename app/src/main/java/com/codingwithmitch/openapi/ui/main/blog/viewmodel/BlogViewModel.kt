package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.codingwithmitch.openapi.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.codingwithmitch.openapi.repository.main.BlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Loading
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.*
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.util.AbsentLiveData
import com.codingwithmitch.openapi.util.PreferencesKey.Companion.BLOG_FILTER
import com.codingwithmitch.openapi.util.PreferencesKey.Companion.BLOG_ORDER
import okhttp3.MediaType
import okhttp3.RequestBody


class BlogViewModel
@ViewModelInject
constructor(
   val blogRepository: BlogRepository,
   val sessionManager: SessionManager,
   val sharedPreferences: SharedPreferences,
   private val editor: SharedPreferences.Editor
): BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(BLOG_FILTER, BLOG_FILTER_DATE_UPDATED)
        )

        setBlogOrder(
            sharedPreferences.getString(BLOG_ORDER, BLOG_ORDER_ASC)
        )
    }


    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent) {
            is BlogSearchEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        query =  getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter() ,
                        page =  getPage()
                    )
                }?:AbsentLiveData.create()
            }

            is CheckAuthorOfBlogPostEvent->{
            return sessionManager.cachedToken.value?.let { authToken ->
                blogRepository.isAuthorOfBlogPost(
                    authToken = authToken,
                    slug = getSlug()
                )
            }?: AbsentLiveData.create()

            }

            is DeleteBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.deleteBlogPost(
                        authToken,
                        getBlogPost()
                    )
                }?:AbsentLiveData.create()
            }

            is None ->{
               return object : LiveData<DataState<BlogViewState>>() {
                   override fun onActive() {
                       super.onActive()
                       value = DataState(
                           null,
                          Loading(false),
                           null
                       )
                   }
               }
            }

            is UpdateBlogPostEvent -> {
                return sessionManager.cachedToken.value?.let {authToken->

                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body)

                    blogRepository.updateBlogPost(
                        authToken,
                        getSlug(),
                        title,
                        body,
                        stateEvent.image
                    )
                }?:AbsentLiveData.create()

                }
            }
        }


    override fun initViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }


    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}