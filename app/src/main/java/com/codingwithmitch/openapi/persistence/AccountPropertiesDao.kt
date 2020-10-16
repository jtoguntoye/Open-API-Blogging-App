package com.codingwithmitch.openapi.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.codingwithmitch.openapi.models.AccountProperties

@Dao
interface AccountPropertiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE )
    fun insertAndReplace(accountProperties: AccountProperties): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(accountProperties: AccountProperties): Long

    @Query("SELECT * FROM account_properties where pk = :pk")
    fun searchByPk(pk: Int): LiveData<AccountProperties?>

    @Query("SELECT * FROM account_properties where email = :email")
    fun searchByEmail(email: String): AccountProperties?

    @Query("UPDATE account_properties SET email = :email, username =  :username WHERE pk =:pk")
    fun updateAccountProperties(pk: Int, email: String, username: String)


}