package com.codingwithmitch.openapi.DI.auth

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

    // TEMPORARY
    @Provides
    fun provideFakeApiService(): OpenApiAuthService{
        return Retrofit.Builder()
            .baseUrl("https://open-api.xyz")
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideAuthRepository(
         authTokenDao: AuthTokenDao,
         accountPropertiesDao: AccountPropertiesDao,
         authApiService: OpenApiAuthService,
         sessionManager: SessionManager
    ):AuthRepository{
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            authApiService,
            sessionManager
        )
    }

}