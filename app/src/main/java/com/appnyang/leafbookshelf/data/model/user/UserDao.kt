package com.appnyang.leafbookshelf.data.model.user

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * User Data Access Object.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-04.
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): LiveData<User>

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)
}
