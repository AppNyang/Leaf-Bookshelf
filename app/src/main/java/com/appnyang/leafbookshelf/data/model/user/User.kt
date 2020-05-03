package com.appnyang.leafbookshelf.data.model.user

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime

/**
 * User entity.
 * User has one-to-many relationship with Collection.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-04.
 */
@Entity(indices = [Index(value = ["email"], unique = true)])
data class User(
    val email: String,
    var displayName: String,
    val createdAt: DateTime
) {
    @PrimaryKey(autoGenerate = true)
    var userId: Long = 0
}
