package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import com.codingwithmitch.openapi.ui.main.blog.BlogViewModel
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.*
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import timber.log.Timber


    fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)

    }


    fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
    Timber.d("BlogViewModel: load FirstPage: ${getSearchQuery()}")
    }

    fun BlogViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page
    update.blogFields.page = page + 1
    setViewState(update)
    }

    fun BlogViewModel.nextPage() {
    if((!getIsQueryExhausted())
        &&(!getQueryInProgress())) {
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent())
    }
}

    fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState) {
    Timber.d("BlogViewModel dataState: ${viewState}")
    Timber.d("BlogViewModel DataState: isQueryInProgress?:"  +
            "${viewState.blogFields.isQueryInProgress}")

    Timber.d("BlogViewModel DataState: isQueryInExhausted?:"  +
            "${viewState.blogFields.isQueryExhausted}")

    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setBlogListData(viewState.blogFields.blogList)
}

