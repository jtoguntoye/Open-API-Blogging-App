package com.codingwithmitch.openapi.ui.auth

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codingwithmitch.openapi.repository.auth.AuthRepository

class AuthViewModel
@ViewModelInject
constructor(
    val authRepository: AuthRepository
): ViewModel(){
}