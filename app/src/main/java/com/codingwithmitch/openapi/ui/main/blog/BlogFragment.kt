package com.codingwithmitch.openapi.ui.main.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogViewState
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.*
import com.codingwithmitch.openapi.util.ErrorHandling
import com.codingwithmitch.openapi.util.TopSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_blog.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BlogFragment : BaseBlogFragment(), BlogListAdapter.Interaction{


    private lateinit var recyclerAdapter: BlogListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initRecyclerView()
        subscribeObservers()

        if(savedInstanceState == null) {
            blogViewModel.loadFirstPage()
        }

    }

    fun initRecyclerView(){
        blog_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)

            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator)  // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = BlogListAdapter(requestManager, this@BlogFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener(){


                // this will be used for setting up pagination later
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if(lastPosition == recyclerAdapter.itemCount.minus(1)){
                        Timber.d("BlogFragment, Attempting to load next page...")
                        //TODO ("Load next page ")
                        blogViewModel.nextPage()
                    }


                }
            })
            adapter = recyclerAdapter
        }
    }



    private fun subscribeObservers() {
        blogViewModel.dataState.observe(viewLifecycleOwner, Observer {dataState->
            if(dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)

            }
        })

        blogViewModel.viewState.observe(viewLifecycleOwner, Observer {viewState ->
            Timber.d("BlogFragment ViewState: ${viewState}")
            if(viewState != null) {
                recyclerAdapter.submitList(
                   list =  viewState.blogFields.blogList,
                     isQueryExhausted = viewState.blogFields.isQueryExhausted
                )
            }
        })
    }

    private fun handlePagination(dataState:DataState<BlogViewState>) {
        //handle incoming data from dataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let {
                    blogViewModel.handleIncomingBlogListData(it)
                }
            }
        }

        //check for pagination end(e.g no more result)
        //must do this because the server will return ApiErrorResponse if page is not valid
        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if(ErrorHandling.isPaginationDone(it)){
                    //handle the error message event so it does not display on the UI
                    event.getContentIfNotHandled()
                    //set query exhausted to update RecyclerView with
                    //"No more results.." list item
                    blogViewModel.setQueryExhausted(true)
                }
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        //clear references to prevent memory leaks
        blog_post_recyclerview.adapter = null
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
       blogViewModel.setBlogPost(item)

        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)

    }
}