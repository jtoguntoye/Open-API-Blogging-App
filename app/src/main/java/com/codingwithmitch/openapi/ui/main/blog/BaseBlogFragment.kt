package com.codingwithmitch.openapi.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.RequestManager
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.DataStateChangeListener
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.BlogViewModel
import timber.log.Timber
import javax.inject.Inject

abstract class BaseBlogFragment : Fragment(){


    @Inject
    lateinit var requestManager: RequestManager

    lateinit var stateChangeListener: DataStateChangeListener

    val blogViewModel: BlogViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Timber.e( "$context must implement DataStateChangeListener" )
        }
    }
    fun setUpActionBarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpActionBarWithNavController(R.id.blogFragment, activity as AppCompatActivity)
        cancelActiveJobs()
    }

    fun cancelActiveJobs(){
        blogViewModel.cancelActiveJobs()
    }
}