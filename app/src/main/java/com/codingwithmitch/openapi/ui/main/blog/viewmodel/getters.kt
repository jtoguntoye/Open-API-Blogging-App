package com.codingwithmitch.openapi.ui.main.blog.viewmodel

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
