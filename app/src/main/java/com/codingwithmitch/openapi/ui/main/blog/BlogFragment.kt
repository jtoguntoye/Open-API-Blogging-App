package com.codingwithmitch.openapi.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
class BlogFragment : BaseBlogFragment(),
    BlogListAdapter.Interaction,
        SwipeRefreshLayout.OnRefreshListener
{


    private lateinit var recyclerAdapter: BlogListAdapter
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

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

    //called when you execute a search query
    private fun onBlogSearchOrFilter() {
        blogViewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        blog_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
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

    private fun setUpSearchView(menu: Menu) {
        activity?.apply{
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true

            // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
            val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
            searchPlate.setOnEditorActionListener{v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                        actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val searchQuery = v.text.toString()
                    Timber.e( "SearchView: (keyboard or arrow) executing search...: ${searchQuery}")
                    blogViewModel.setQuery(searchQuery).let {
                        onBlogSearchOrFilter()
                    }
                }
                true
            }

            // SEARCH BUTTON CLICKED (in toolbar)
            (searchView.findViewById(R.id.search_go_btn) as View).setOnClickListener {
                val searchQuery = searchPlate.text.toString()
                Timber.e("search view button executing ...$searchQuery")
                blogViewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }

            }


        }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        setUpSearchView(menu)

    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }
}