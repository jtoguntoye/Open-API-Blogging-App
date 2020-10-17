package com.codingwithmitch.openapi.ui.main.account


import android.accounts.Account
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent
import com.codingwithmitch.openapi.util.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import kotlinx.android.synthetic.main.fragment_change_password.*
import timber.log.Timber

class ChangePasswordFragment : BaseAccountFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        update_password_button.setOnClickListener {
            accountViewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                 input_current_password.text.toString(),
                    input_new_password.text.toString(),
                    input_confirm_new_password.text.toString()
                )
            )
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        accountViewModel.dataState.observe(viewLifecycleOwner, Observer {dataState->
            stateChangeListener.onDataStateChange(dataState)
            Timber.d("ChangePasswordFragment, DataState: $dataState")
            if(dataState!= null) {
                dataState.data?.let {data->
                    data.response?.let{event->
                           if(event.peekContent()
                                   .message
                                   .equals(RESPONSE_PASSWORD_UPDATE_SUCCESS)){
                               stateChangeListener.hideSoftKeyboard()
                                       findNavController().popBackStack()
                                   }
                    }
                }
            }
        })
    }
}