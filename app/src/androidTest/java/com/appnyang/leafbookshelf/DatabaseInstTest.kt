package com.appnyang.leafbookshelf

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appnyang.leafbookshelf.data.model.AppDatabase
import com.appnyang.leafbookshelf.data.model.user.User
import com.appnyang.leafbookshelf.data.model.user.UserDao
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Instrumented database test.
 *
 * @author Sangwoo <sangwoo@yesang.com> on 2020-05-03.
 */

abstract class DatabaseInstTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    lateinit var userDao: UserDao

    @Before
    fun initialize() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        JodaTimeAndroid.init(context)

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        userDao = db.getUserDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}

/**
 * Tests of UserDao.
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest : DatabaseInstTest() {

    @Test
    @Throws(Exception::class)
    fun hasNullUser() {
        val readUser = userDao.getUser().getValueBlocking()

        assertThat("readUser should be null", readUser, nullValue())
    }

    @Test
    @Throws(Exception::class)
    fun createUserAndRead() {
        val user = User("test-user@domain.com", "Twist", DateTime.now())

        userDao.insert(user)

        val readUser = userDao.getUser().getValueBlocking()

        assertThat("Should two users are same", readUser, equalTo(user))
    }
}

@Throws(InterruptedException::class)
fun <T> LiveData<T>.getValueBlocking(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val innerObserver = Observer<T> {
        value = it
        latch.countDown()
    }

    observeForever(innerObserver)

    latch.await(2, TimeUnit.SECONDS)

    return value
}
