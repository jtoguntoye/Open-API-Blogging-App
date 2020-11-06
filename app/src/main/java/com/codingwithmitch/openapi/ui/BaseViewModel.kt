package com.codingwithmitch.openapi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import timber.log.Timber

abstract class BaseViewModel<StateEvent, ViewState>: ViewModel() {

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val viewState: LiveData<ViewState>
    get() = _viewState

    val dataState: LiveData<DataState<ViewState>> = Transformations
        .switchMap(_stateEvent){stateEvent ->
            stateEvent?.let{
                handleStateEvent(stateEvent)
            }
        }

    fun setStateEvent(stateEvent: StateEvent) {
        _stateEvent.value = stateEvent
    }

    fun setViewState(viewState: ViewState){
        _viewState.value = viewState
    }
    fun getCurrentViewStateOrNew(): ViewState {
        Timber.d("getCurrentViewStateOrNew method called")
        val value = viewState.value?.let{
            it
        }?: initViewState()
    return value
    }

   abstract fun initViewState(): ViewState

    abstract fun handleStateEvent(stateEvent: StateEvent):LiveData<DataState<ViewState>>

}