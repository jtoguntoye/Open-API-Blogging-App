package com.codingwithmitch.openapi.ui.main.account

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.repository.main.AccountRepository
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent.*
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import com.codingwithmitch.openapi.util.AbsentLiveData

class AccountViewModel
@ViewModelInject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
    ): BaseViewModel<AccountStateEvent, AccountViewState>() {


    override fun initViewState(): AccountViewState {
        return AccountViewState()
    }

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
       when(stateEvent) {
           is GetAccountPropertiesEvent -> {
            return sessionManager.cachedToken.value?.let { authToken ->
                accountRepository.getAccountProperties(authToken)
            }?:AbsentLiveData.create()
           }

           is UpdateAccountPropertiesEvent -> {
               return sessionManager.cachedToken.value?.let { authToken ->
                   authToken.account_pk?.let { pk ->
                       val newAccountProperties = AccountProperties(
                           pk,
                           stateEvent.email,
                           stateEvent.username
                       )
                       accountRepository.saveAccountProperties(
                           authToken,
                           newAccountProperties
                       )
                   }
               }?: AbsentLiveData.create()
           }

           is ChangePasswordEvent -> {
               return AbsentLiveData.create()
           }
           is None -> {
               return AbsentLiveData.create()
           }
       }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logOut()
    }


}