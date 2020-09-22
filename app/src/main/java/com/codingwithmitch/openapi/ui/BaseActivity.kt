package com.codingwithmitch.openapi.ui

import androidx.appcompat.app.AppCompatActivity
import com.codingwithmitch.openapi.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity: AppCompatActivity(), DataStateChangeListener {

    @Inject
    lateinit var sessionManager: SessionManager


    override fun onDataStateChange(dataState: DataState<*>?) {
      dataState?.let {
          GlobalScope.launch(Main) {
              displayProgressBar(it.loading?.isLoading)
          }
          it.error?.let {errorEvent ->
              handleStateError(errorEvent)
          }

          it.data?.let{
              it.response?.let {responseEvent->
                  handleStateResponse(responseEvent)
              }
          }
      }
    }

    private fun handleStateResponse(responseEvent: Event<Response>) {
        responseEvent.getContentIfNotHandled()?.let{
            when(it.responseType) {
                is ResponseType.Toast -> {
                    it.message?.let {message->
                        displayToast(message)
                    }


                }
                is ResponseType.Dialog -> {
                    it.message?.let{message ->
                        displaySuccessDialog(message)
                    }
                }
                is ResponseType.None -> {
                    Timber.e("HandleStateResponse: ${it.message}")
                }
            }
        }
    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let{
            when(it.response.responseType) {
                is ResponseType.Toast -> {
                    it.response.message?.let {message->
                        displayToast(message)
                    }


                }
                is ResponseType.Dialog -> {
                it.response.message?.let{message ->
                    displayErrorDialog(message)
                }
                }
                is ResponseType.None -> {
                    Timber.i("HandleStateError: ${it.response.message}")
                }
            }
        }
    }

    abstract fun displayProgressBar(boolean: Boolean?)
}