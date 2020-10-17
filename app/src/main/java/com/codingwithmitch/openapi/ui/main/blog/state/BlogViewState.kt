package com.codingwithmitch.openapi.ui.main.blog.state

import com.codingwithmitch.openapi.models.BlogPost

data class BlogViewState(
    var blogFields: BlogFields = BlogFields()

// to add
//viewBlog fragment vars
//updateBogFragment vars
) {


    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = ""
    )
}