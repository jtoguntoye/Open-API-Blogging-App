package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

abstract class BaseAuthFragment: Fragment() {

    val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelActiveJobs()
    }

     fun cancelActiveJobs(){
        authViewModel.cancelActiveJobs()
    }
}