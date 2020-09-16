package com.codingwithmitch.openapi.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(val authTokenDao: AuthTokenDao,
            val application: Application
)
{

    private val _cachedToken = MutableLiveData<AuthToken>()

    val cachedToken: LiveData<AuthToken>
    get() = _cachedToken

    fun login(newValue: AuthToken) {
        setValue(newValue)
    }

    fun logOut() {
        Timber.d("logout...")

        GlobalScope.launch(IO) {
            var errorMessage: String? = null
            try {
                _cachedToken.value!!.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                }

            }
            catch (e: CancellationException) {
                Timber.d("logout: ${e.message}")
                errorMessage = e.message
            }
            catch(e: Exception){
                Timber.d("logout: ${e.message}")
                errorMessage = errorMessage + "\n" + e.message
            }
            finally {
                errorMessage?.let{
                    Timber.d("logout: ${errorMessage}" )
                }
                Timber.d("Logout... finally")
                setValue(null)
            }
        }

    }
    fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Main) {
            if (_cachedToken.value != newValue) {
                _cachedToken.value = newValue
            }
        }
    }

    fun isConectedToTheInternet(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
        return cm.activeNetworkInfo.isConnected

        } catch (e: Exception) {
            Timber.d("ConnectivityCheck: ${e.message} ")
        }
        return false
    }

}