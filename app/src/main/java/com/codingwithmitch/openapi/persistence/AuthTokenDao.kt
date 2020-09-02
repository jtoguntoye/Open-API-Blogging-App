package com.codingwithmitch.openapi.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingwithmitch.openapi.models.AuthToken

//to describe methods for accessing the local table that stores user authentication Tokens
@Dao
interface AuthTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewToken(authToken:AuthToken): Long

    //method used for logging out the user. the user's token is set to null in the table
    @Query("UPDATE auth_token SET token = null WHERE account_pk = :pk ")
    fun nullifyToken(pk: Int): Int
}