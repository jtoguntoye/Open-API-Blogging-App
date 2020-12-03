package com.codingwithmitch.openapi.ui.main.blog.state

import android.net.Uri
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.codingwithmitch.openapi.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED

data class BlogViewState(
    var blogFields: BlogFields = BlogFields(),
    var viewBlogFields: ViewBlogFields = ViewBlogFields(),
    var updatedBlogFields: UpdatedBlogFields = UpdatedBlogFields()

// to add
//viewBlog fragment vars
//updateBogFragment vars
) {


    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = "",
        var page: Int = 1 ,
        var isQueryExhausted: Boolean = false,
        var isQueryInProgress: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = BLOG_ORDER_ASC

    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlogPost: Boolean = false
    )

    data class UpdatedBlogFields(
        var updatedBlogTitle: String? = null,
        var updatedBlogBody: String? = null,
        var updatedImageUri: Uri? = null
    )
}