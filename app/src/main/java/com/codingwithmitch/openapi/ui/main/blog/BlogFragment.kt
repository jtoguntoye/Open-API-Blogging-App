package com.codingwithmitch.openapi.ui.main.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_blog.*
import timber.log.Timber

@AndroidEntryPoint
class BlogFragment : BaseBlogFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goViewBlogFragment.setOnClickListener {
            findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
        }
        subscribeObservers()
        executeSearch()

    }

    fun executeSearch(){
        blogViewModel.setQuery("")
        blogViewModel.setStateEvent(BlogStateEvent.BlogSearchEvent())
    }


    fun subscribeObservers() {
        blogViewModel.dataState.observe(viewLifecycleOwner, Observer {dataState->
            if(dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                dataState.data?.let{
                it.data?.let {event ->
                    event.getContentIfNotHandled()?.let {blogViewState->
                        blogViewModel.setBlogListData(blogViewState.blogFields.blogList)
                        }

                    }
                }
            }
        })

        blogViewModel.viewState.observe(viewLifecycleOwner, Observer {viewState ->
            Timber.d("BlogFragment ViewState: ${viewState}")
        })
    }
}