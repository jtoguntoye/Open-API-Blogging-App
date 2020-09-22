package com.codingwithmitch.openapi.ui

import androidx.appcompat.app.AppCompatActivity
import com.codingwithmitch.openapi.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity: AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

}