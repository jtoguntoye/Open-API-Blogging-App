package com.codingwithmitch.openapi.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_response.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_response.RegistrationResponse
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.util.GenericApiResponse
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val authApiService: OpenApiAuthService,
    val sessionManager: SessionManager
){

    fun testLoginRequest(email: String, password: String): LiveData<GenericApiResponse<LoginResponse>> {
        return authApiService.login(email,password)
    }

    fun testRegistrationRequest(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<GenericApiResponse<RegistrationResponse>> {
        return authApiService.register(email, username, password, confirmPassword)
    }
}