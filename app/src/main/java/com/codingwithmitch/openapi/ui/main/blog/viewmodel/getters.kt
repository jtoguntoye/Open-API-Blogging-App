package com.codingwithmitch.openapi.ui.main.blog.viewmodel

import com.codingwithmitch.openapi.models.BlogPost

fun BlogViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryExhausted
    }
    }

    fun BlogViewModel.getQueryInProgress(): Boolean{
        getCurrentViewStateOrNew().let {
            return it.blogFields.isQueryInProgress
        }
    }

     fun BlogViewModel.getPage(): Int{
            getCurrentViewStateOrNew().let{
                return it.blogFields.page
            }
     }

     fun BlogViewModel.getSearchQuery(): String{
            getCurrentViewStateOrNew().let{
                return it.blogFields.searchQuery
            }
     }

    fun BlogViewModel.getFilter(): String {
        getCurrentViewStateOrNew().let {
            return it.blogFields.filter
        }
    }
    fun BlogViewModel.getOrder(): String {
        getCurrentViewStateOrNew().let {
        return it.blogFields.order
        }
    }

    fun  BlogViewModel.getSlug(): String {
        getCurrentViewStateOrNew().let {
            it.viewBlogFields.blogPost?.let {
                return it.slug
            }
        }
        return ""
    }

    fun BlogViewModel.isAuthorOfBlogPost(): Boolean {
        getCurrentViewStateOrNew().let {
            return it.viewBlogFields.isAuthorOfBlogPost
        }
    }

     fun BlogViewModel.getBlogPost(): BlogPost {
         getCurrentViewStateOrNew().let {
             return  it.viewBlogFields.blogPost?.let {
                 return it
             }?: getDummyBlogPost()
         }
     }



    fun BlogViewModel.getDummyBlogPost():  BlogPost {
    return BlogPost(-1, "", "","", "",1, "" )
    }






