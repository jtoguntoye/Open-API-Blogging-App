package com.codingwithmitch.openapi.ui.main.blog.state

import okhttp3.MultipartBody

sealed class BlogStateEvent {

    class BlogSearchEvent : BlogStateEvent()

    class None: BlogStateEvent()

    class CheckAuthorOfBlogPostEvent: BlogStateEvent()

    class DeleteBlogPostEvent: BlogStateEvent()

    class UpdateBlogPostEvent(
        var title: String,
        var body: String,
        val image: MultipartBody.Part?
    ): BlogStateEvent()
}