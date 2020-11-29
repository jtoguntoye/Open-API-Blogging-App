package com.codingwithmitch.openapi.ui

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.codingwithmitch.openapi.session.SessionManager
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity: AppCompatActivity(),
    DataStateChangeListener,
UICommunicationListener{

    @Inject
    lateinit var sessionManager: SessionManager


    override fun onUIMessageReceived(uiMessage: UIMessage) {

        when(uiMessage.messageType){
            is UIMessageType.AreYouSureDialog -> {
             areYouSureDialog(uiMessage.message,uiMessage.messageType.callback)
            }

            is UIMessageType.Dialog -> {
            displayInfoDialog(uiMessage.message)
            }

            is UIMessageType.Toast ->{
                displayToast(uiMessage.message)
            }
            is UIMessageType.None -> {
                Timber.i("OnMEssageReceived: ${uiMessage.message}")
            }

        }
    }

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

    override fun hideSoftKeyboard() {
        if(currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager
                .hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

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