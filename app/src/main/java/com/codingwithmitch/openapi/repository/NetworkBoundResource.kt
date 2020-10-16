package com.codingwithmitch.openapi.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.util.Constants.Companion.NETWORK_TIMEOUT
import com.codingwithmitch.openapi.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.codingwithmitch.openapi.util.ErrorHandling
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.codingwithmitch.openapi.util.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.codingwithmitch.openapi.util.GenericApiResponse.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import timber.log.Timber

abstract class NetworkBoundResource<ResponseObject,CachedObject ,ViewStateType>
    (
    isNetworkAvailable: Boolean , // is there a network connection
    isNetworkRequest: Boolean, //is this a network request
    shouldCancelIfNoInternet: Boolean, //should this job be cancelled if there is no network
    shouldLoadFromCache: Boolean //should the cached data be loaded?
){
    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if(shouldLoadFromCache){
            // view cache to start
            val dbSource = loadFromCache()
            result.addSource(dbSource){
                result.removeSource(dbSource)
                setValue(DataState.loading(isLoading = true, cachedData = it))
            }
        }

        if(isNetworkRequest){
            if(isNetworkAvailable){
                doNetworkRequest()
            }
            else{
                if(shouldCancelIfNoInternet){
                    onErrorReturn(
                        ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET,
                        shouldUseDialog =  true,
                        shouldUseToast  = false)
                }
                else{
                    doCacheRequest()
                }
            }
        }
        else{
            doCacheRequest()
        }
    }




    fun doCacheRequest(){
        coroutineScope.launch {

            //fake delay for testing cache
            delay(TESTING_NETWORK_DELAY)

            // view data from cache only and return
            createCacheRequestAndReturn()
        }
    }

    fun doNetworkRequest(){
        coroutineScope.launch {
            // simulate a network delay for testing
            delay(TESTING_NETWORK_DELAY)

            withContext(Main) {
                val apiResponse = createCall()
                result.addSource(apiResponse) {response ->
                    result.removeSource(apiResponse)

                    coroutineScope.launch {
                        handleNetworkCall(response)
                    }
                }
            }
        }
        GlobalScope.launch(IO) {
            delay(NETWORK_TIMEOUT)
            if(!job.isCompleted) {
                Timber.d("Network bound resource: JOB NETWORK TIMEOUT ")
                job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
            }
        }
    }



    suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {
        when(response) {
            is ApiSuccessResponse ->{
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse -> {
                Timber.d("Network bound resource: ${response.errorMessage}")
                onErrorReturn(response.errorMessage, true, false)
            }
            is ApiEmptyResponse -> {
                Timber.d("Network bound resource:request returned NOTHING")
                onErrorReturn("HTTP 204 RETURNED NOTHING", true, false)
            }

        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {
        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()
        if(msg == null) {
            msg = ERROR_UNKNOWN
        }
        else if(ErrorHandling.isNetworkError(msg)) {
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }
        if(shouldUseDialog) {
            responseType = ResponseType.Dialog()
        }
        if(shouldUseToast) {
            responseType = ResponseType.Toast()
        }
        //TODO ("complete the job and emit the new data state")
        onCompleteJob(DataState.error(
            response = Response(
                message = msg,
                responseType = responseType
            )
        ))
    }

    @UseExperimental(InternalCoroutinesApi::class)
    private fun initNewJob(): Job {
        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object: CompletionHandler {
            override fun invoke(cause: Throwable?) {
                if(job.isCancelled) {
                    Timber.d("NetworkBoundResource: Job has been cancelled")
                    cause?.let{
                        onErrorReturn(it.message,false, true)
                    }?: onErrorReturn(ERROR_UNKNOWN, false, true)
                }
                else if(job.isCompleted) {
                    Timber.d("job is completed")
                    //Do nothing. should be handled already
                }
            }
        })
        coroutineScope = CoroutineScope(IO +job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun createCacheRequestAndReturn()

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun loadFromCache(): LiveData<ViewStateType>

    abstract suspend fun updateLocalDb(cacheObject: CachedObject?)

    abstract fun setJob(job: Job)

}