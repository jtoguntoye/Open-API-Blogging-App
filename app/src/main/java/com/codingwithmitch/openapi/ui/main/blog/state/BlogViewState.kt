package com.codingwithmitch.openapi.ui.main.blog.state

import com.codingwithmitch.openapi.models.BlogPost

data class BlogViewState(
    var blogFields: BlogFields = BlogFields(),
    var viewBlogFields: ViewBlogFields = ViewBlogFields()

// to add
//viewBlog fragment vars
//updateBogFragment vars
) {


    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = ""
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthor: Boolean = false
    )
}