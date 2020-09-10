package com.codingwithmitch.openapi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

abstract class BaseViewModel<StateEvent, ViewState>: ViewModel() {

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val viewState: LiveData<ViewState>
    get() = _viewState

    val dataState: LiveData<DataState<ViewState>> = Transformations
        .switchMap(_stateEvent){stateEvent ->
            stateEvent?.let{
                HandleStateEvent(stateEvent)
            }
        }

    fun setStateEvent(stateEvent: StateEvent) {
        _stateEvent.value = stateEvent
    }

    fun getCurrentViewStateOrNew(): ViewState {
        val value = viewState.value?.let{
            it
        }?: initViewState()
    return value
    }

   abstract fun initViewState(): ViewState

    abstract fun HandleStateEvent(stateEvent: StateEvent):LiveData<DataState<ViewState>>

}