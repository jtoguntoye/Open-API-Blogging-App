package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import android.net.Uri
import com.codingwithmitch.openapi.models.BlogPost


fun BlogViewModel.setQuery(query: String) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.searchQuery = query
    setViewState(update)
    }

    fun BlogViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryExhausted = isExhausted
        setViewState(update)
    }
    fun BlogViewModel.setQueryInProgress(isInProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryInProgress = isInProgress
    setViewState(update)
    }

    fun BlogViewModel.setBlogListData(blogList: List<BlogPost>) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.blogList = blogList
    setViewState(update)
    }

    fun BlogViewModel.setBlogPost(blogPost: BlogPost) {
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.blogPost = blogPost
    setViewState(update)
    }

    fun BlogViewModel.setIsAuthorOfBlogPost(isAuthorOfBlogPost: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.isAuthorOfBlogPost = isAuthorOfBlogPost
    setViewState(update)
    }

fun BlogViewModel.setBlogFilter(filter: String?) {
    filter?.let{
        val update = getCurrentViewStateOrNew()
        update.blogFields.filter = filter
        setViewState(update)
    }
}

    fun BlogViewModel.setBlogOrder(order: String) {
        val update = getCurrentViewStateOrNew()
        update.blogFields.order = order
        setViewState(update)
    }

    fun BlogViewModel.removeDeletedBlogPost(){
        val update = getCurrentViewStateOrNew()
         val list= update.blogFields.blogList.toMutableList()

        for(i in 0..(list.size-1)) {
            if(list[i] == getBlogPost()) {
                list.remove(getBlogPost())
                break
            }
        }
        setBlogListData(list)

    }

    fun BlogViewModel.setUpdatedBlogFields(title: String?, body: String?, uri: Uri?){
        val update = getCurrentViewStateOrNew()
        val updatedBlogFields = update.updatedBlogFields
        title?.let{updatedBlogFields.updatedBlogTitle}
        body?.let{updatedBlogFields.updatedBlogBody}
        uri?.let{updatedBlogFields.updatedImageUri}
        update.updatedBlogFields = updatedBlogFields

        setViewState(update)
    }

    fun BlogViewModel.updateListItem(newBlogPost: BlogPost){

    val update = getCurrentViewStateOrNew()
    var list = update.blogFields.blogList.toMutableList()

    for(i in 0..(list.size-1)){
        if(list[i].pk == newBlogPost.pk) {
            list[i] = newBlogPost
            break
        }
    }
    update.blogFields.blogList = list
    setViewState(update)
    }

    fun BlogViewModel.onBlogUpdateSuccess(blogPost: BlogPost) {
        setUpdatedBlogFields(
            title = blogPost.title,
            body = blogPost.body,
            uri = null
        )//update UpdateBlogFragment (not necessary since the fragment will be popped from  backStack
        // when update is successful

        setBlogPost(blogPost) //update viewBlogFragment
        updateListItem(blogPost) // update blogFragment
    }


