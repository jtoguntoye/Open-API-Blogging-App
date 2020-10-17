package com.codingwithmitch.openapi.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.ui.main.account.state.AccountStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_account.*
import timber.log.Timber

@AndroidEntryPoint
class AccountFragment : BaseAccountFragment(){




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        change_password.setOnClickListener{
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }

        logout_button.setOnClickListener {
       accountViewModel.logout()
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        accountViewModel.dataState.observe(viewLifecycleOwner, Observer {dataState->
            stateChangeListener.onDataStateChange(dataState)
            if(dataState != null){
                dataState.data?.let{data->
                    data.data?.let{event->
                        event.getContentIfNotHandled()?.let{accountViewState ->
                            accountViewState.accountProperties?.let{accountProperties ->
                                Timber.d("AccountFragment, DataState: ${accountProperties}")
                                accountViewModel.setAccountPropertiesData(accountProperties)

                            }

                        }
                    }
                }
            }
        })

        accountViewModel.viewState.observe(viewLifecycleOwner, Observer { accountViewState->
            if(accountViewState!=null) {
                accountViewState.accountProperties?.let {
                    Timber.d("AccountFragment, ViewState: ${it}")
                    setAccountDataFields(it)
                }
            }
        })




    }

    override fun onResume() {
        super.onResume()
        accountViewModel.setStateEvent(AccountStateEvent.GetAccountPropertiesEvent())
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        email?.text = accountProperties.email
        username?.text = accountProperties.username
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit->{
                 findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
