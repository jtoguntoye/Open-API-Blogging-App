package com.codingwithmitch.openapi.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.models.BlogPost
import com.codingwithmitch.openapi.ui.AreYouSureCallback
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.CheckAuthorOfBlogPostEvent
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent.DeleteBlogPostEvent
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.*
import com.codingwithmitch.openapi.util.DateUtils
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_view_blog.*
import timber.log.Timber
import java.lang.Exception

@AndroidEntryPoint
class ViewBlogFragment : BaseBlogFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        stateChangeListener.expandAppBar()

        delete_button.setOnClickListener {
            deleteBlogPost()
        }
    }

    private fun deleteBlogPost() {
        blogViewModel.setStateEvent(
            DeleteBlogPostEvent()
        )
    }

    fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback{
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {
                //ignore
            }
        }
    }

    fun checkIsAuthorOfBlogPost() {
        blogViewModel.setIsAuthorOfBlogPost(false) //reset
        blogViewModel.setStateEvent(CheckAuthorOfBlogPostEvent())
    }

    private fun subscribeObservers() {
        blogViewModel.dataState.observe(viewLifecycleOwner, Observer {dataState->
            stateChangeListener.onDataStateChange(dataState)

            dataState.data?.let {data ->
                data.data?.getContentIfNotHandled()?.let {viewState->
                    blogViewModel.setIsAuthorOfBlogPost(
                        viewState.viewBlogFields.isAuthorOfBlogPost
                    )
                }

                data.response?.peekContent()?.let{response->
                    if(response.message.equals(SUCCESS_BLOG_DELETED)) {
                        blogViewModel.removeDeletedBlogPost()
                        findNavController().popBackStack()
                    }
                }
            }
        })

        blogViewModel.viewState.observe(viewLifecycleOwner, Observer { viewState->
            viewState.viewBlogFields.blogPost?.let { blogPost ->
                setBlogProperties(blogPost)
            }

            if(viewState.viewBlogFields.isAuthorOfBlogPost) {
                adaptViewToAuthorMode()
            }
        })
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        delete_button.visibility = View.VISIBLE
    }

    fun setBlogProperties(blogPost: BlogPost) {
        requestManager.
                load(blogPost.image)
                .into(blog_image)

        blog_title.text = blogPost.title
        blog_body.text = blogPost.body
        blog_author.text = blogPost.username
        blog_update_date.text = DateUtils.convertLongToStringDate(blogPost.date_updated)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if(blogViewModel.isAuthorOfBlogPost()){

            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(blogViewModel.isAuthorOfBlogPost()){
            when(item.itemId){
                R.id.edit-> {
                    navUpdateBlogFragment()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }



    fun navUpdateBlogFragment() {

        try {
            //prep for next fragment
            blogViewModel.setUpdatedBlogFields(
                blogViewModel.getBlogPost().title,
                blogViewModel.getBlogPost().body,
                blogViewModel.getBlogPost().image.toUri()
            )
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)

        }catch (e: Exception){
            Timber.e("Exception: ${e.message}")
        }

    }
}

