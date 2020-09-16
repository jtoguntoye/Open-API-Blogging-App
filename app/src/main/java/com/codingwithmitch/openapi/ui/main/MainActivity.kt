package com.codingwithmitch.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.auth.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    @Inject
    lateinit var  sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tool_bar.setOnClickListener{
            sessionManager.logOut()
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer {authToken->
        Timber.d("MainActivity subscriber: AuthToken: $authToken")
            if(authToken==null || authToken.account_pk == -1 || authToken.token == null)
            navigateBackToAuthActivity()
        })
    }

    private fun navigateBackToAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}