package com.codingwithmitch.openapi.ui.main.create_blog

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.codingwithmitch.openapi.ui.DataStateChangeListener

abstract class BaseCreateBlogFragment : Fragment(){

    val TAG: String = "AppDebug"

    lateinit var stateChangeListener: DataStateChangeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Log.e(TAG, "$context must implement DataStateChangeListener" )
        }
    }
}