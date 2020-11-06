package com.codingwithmitch.openapi.ui.main.blog

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.repository.main.BlogRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Loading
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.getPage
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.getSearchQuery
import com.codingwithmitch.openapi.util.AbsentLiveData



class BlogViewModel
@ViewModelInject
constructor(
   val blogRepository: BlogRepository,
   val sessionManager: SessionManager,
   val sharedPreferences: SharedPreferences,
   val requestManager: RequestManager
): BaseViewModel<BlogStateEvent, BlogViewState>() {


    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent) {
            is BlogStateEvent.BlogSearchEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        query =  getSearchQuery(),
                        page =  getPage()
                    )
                }?:AbsentLiveData.create()
            }

            is BlogStateEvent.CheckAuthorOfBlogPostEvent->{
            return AbsentLiveData.create()
            }

            is BlogStateEvent.None ->{
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
        }
    }



    override fun initViewState(): BlogViewState {
        return BlogViewState()
    }



    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData(){
        setStateEvent(BlogStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}