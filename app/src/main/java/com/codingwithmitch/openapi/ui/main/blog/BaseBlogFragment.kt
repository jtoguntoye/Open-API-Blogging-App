package com.codingwithmitch.openapi.ui.main.blog

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.codingwithmitch.openapi.ui.DataStateChangeListener
import timber.log.Timber

abstract class BaseBlogFragment : Fragment(){



    lateinit var stateChangeListener: DataStateChangeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Timber.e( "$context must implement DataStateChangeListener" )
        }
    }
}