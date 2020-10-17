package com.codingwithmitch.openapi.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.DataStateChangeListener
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.ForgotPasswordFragment.WebInterface.OnWebInteractionCallback
import com.codingwithmitch.openapi.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_forgot_password2.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.ClassCastException
import java.lang.Error

@AndroidEntryPoint
class ForgotPasswordFragment : BaseAuthFragment() {

     lateinit var webview: WebView
     lateinit var stateChangeListener: DataStateChangeListener

     private val webInteractionCallback: OnWebInteractionCallback =  object : OnWebInteractionCallback{
          override fun onSuccess(email: String) {
               Timber.d("onSuccess: A reset link will be sent to $email")
               onPasswordResetLinkSent()
          }

          override fun onLoading(isLoading: Boolean) {
               Timber.d("onLoading....")
               GlobalScope.launch(Main) {
                    stateChangeListener.onDataStateChange(
                         DataState.loading(isLoading, null)
                    )
               }
          }

          override fun onError(errorMessage: String) {
               val dataState = DataState.error<Any>(
                    response = Response(errorMessage, ResponseType.Dialog())
               )
               Timber.e("error: $errorMessage")
               stateChangeListener.onDataStateChange(
                    dataState = dataState
               )
          }
     }

     private fun onPasswordResetLinkSent() {
          GlobalScope.launch(Main ) {
               parent_view.removeView(webview)
               webview.destroy()

               val animation = TranslateAnimation(password_reset_done_container.width.toFloat(),
               0f,
               0f,
               0f
               )

               animation.duration = 500
               password_reset_done_container.startAnimation(animation)
               password_reset_done_container.visibility = View.VISIBLE
          }
     }

     override fun onCreateView(
          inflater: LayoutInflater, container: ViewGroup?,
          savedInstanceState: Bundle?
     ): View? {
          // Inflate the layout for this fragment
          return inflater.inflate(R.layout.fragment_forgot_password2, container, false)
     }


     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)
          webview = view.findViewById(R.id.webview)
          Timber.d("Forgot password fragment: ${authViewModel}")
          loadPasswordResetWebview()

          return_to_launcher_fragment.setOnClickListener {
               findNavController().popBackStack()

          }
     }


     @SuppressLint("SetJavaScriptEnabled")
     fun loadPasswordResetWebview() {
     stateChangeListener.onDataStateChange(
          DataState.loading(
               isLoading = true,
               cachedData = null
          )
     )
          webview.webViewClient = object : WebViewClient(){
               override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    stateChangeListener.onDataStateChange(
                         DataState.loading(isLoading = false, cachedData = null)
                    )
               }
          }
          webview.loadUrl(Constants.PASSWORD_RESET_URL)
          webview.settings.javaScriptEnabled = true
          webview.addJavascriptInterface(WebInterface(webInteractionCallback), "AndroidTextListener")
     }
     override fun onAttach(context: Context) {
          super.onAttach(context)
          try{
           stateChangeListener = context as DataStateChangeListener
          }
          catch (e: ClassCastException) {
               Timber.e("$context must implement DataStateChangeListener.")
          }
     }


     class WebInterface constructor(
          private val callback: OnWebInteractionCallback
     ) {

          @JavascriptInterface
          fun onSuccess(email: String) {
               callback.onSuccess(email)
          }

          @JavascriptInterface
          fun onError(errorMessage: String) {
               callback.onError(errorMessage)
          }

          @JavascriptInterface
          fun onLoading(isLoading: Boolean) {
               callback.onLoading(isLoading)
          }

          interface OnWebInteractionCallback {
               fun onSuccess(email: String)
               fun onLoading(isLoading: Boolean)
               fun onError(errorMessage: String)
          }

     }
}