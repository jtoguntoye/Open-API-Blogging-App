package com.codingwithmitch.openapi.ui.main.blog

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.main.blog.state.BlogStateEvent
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.onBlogUpdateSuccess
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.setUpdatedBlogFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_update_blog.*
import kotlinx.android.synthetic.main.layout_blog_filter.*
import okhttp3.MultipartBody
import retrofit2.http.Body

@AndroidEntryPoint
class UpdateBlogFragment : BaseBlogFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    fun subscribeObservers() {
        blogViewModel.dataState.observe(viewLifecycleOwner, Observer {dataState->

            stateChangeListener.onDataStateChange(dataState)
            dataState.data?.let {data->
                data.data?.getContentIfNotHandled()?.let {viewState->

                    //if it is not null, the blogpost was updated
                    viewState.viewBlogFields.blogPost?.let {blogPost->
                        blogViewModel.onBlogUpdateSuccess(blogPost).let {
                            findNavController().popBackStack() //navigate back to viewBlogFragment
                        }
                    }
                }
            }
        })

            blogViewModel.viewState.observe(viewLifecycleOwner, Observer {blogViewState->
                blogViewState.updatedBlogFields.let {updatedBlogFields ->
                    setBlogProperties(
                        updatedBlogFields.updatedBlogTitle,
                        updatedBlogFields.updatedBlogBody,
                        updatedBlogFields.updatedImageUri
                    )

                }

            })


    }

    private fun setBlogProperties(
        title: String?, body: String?, image: Uri?) {
        requestManager
            .load(image)
            .into(blog_image)

        blog_title.setText(title)
        blog_body.setText(body)
    }

    private fun saveChanges(){
        var multiPartBody: MultipartBody.Part? = null
        blogViewModel.setStateEvent(
            BlogStateEvent.UpdateBlogPostEvent(
                blog_title.text.toString(),
                blog_body.text.toString(),
                multiPartBody
            )
        )

        stateChangeListener.hideSoftKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.save ->{
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        blogViewModel.setUpdatedBlogFields(
            blog_title.text.toString(),
            blog_body.text.toString(),
            null
        )
    }
}