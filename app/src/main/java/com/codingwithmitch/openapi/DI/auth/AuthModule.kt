package com.codingwithmitch.openapi.DI.auth

import android.content.SharedPreferences
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class AuthModule {


    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): OpenApiAuthService{
        return retrofitBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideAuthRepository(
         authTokenDao: AuthTokenDao,
         accountPropertiesDao: AccountPropertiesDao,
         authApiService: OpenApiAuthService,
         sessionManager: SessionManager,
         sharedPreferences: SharedPreferences,
         editor: SharedPreferences.Editor
    ):AuthRepository{
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            authApiService,
            sessionManager,
            sharedPreferences,
            editor
        )
    }

}