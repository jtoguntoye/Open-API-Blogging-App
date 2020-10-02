package com.codingwithmitch.openapi.ui.main.account

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.codingwithmitch.openapi.ui.DataStateChangeListener

abstract class BaseAccountFragment: Fragment(){

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