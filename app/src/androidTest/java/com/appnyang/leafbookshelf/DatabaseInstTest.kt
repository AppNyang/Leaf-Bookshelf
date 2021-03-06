package com.appnyang.leafbookshelf

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appnyang.leafbookshelf.data.model.AppDatabase
import com.appnyang.leafbookshelf.data.model.CollectionBookCrossRef
import com.appnyang.leafbookshelf.data.model.CollectionWithBooksDao
import com.appnyang.leafbookshelf.data.model.book.Book
import com.appnyang.leafbookshelf.data.model.book.BookDao
import com.appnyang.leafbookshelf.data.model.bookmark.Bookmark
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkDao
import com.appnyang.leafbookshelf.data.model.bookmark.BookmarkType
import com.appnyang.leafbookshelf.data.model.collection.Collection
import com.appnyang.leafbookshelf.data.model.collection.CollectionDao
import com.appnyang.leafbookshelf.data.model.user.User
import com.appnyang.leafbookshelf.data.model.user.UserDao
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.*
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
    lateinit var collectionDao: CollectionDao
    lateinit var bookDao: BookDao
    lateinit var bookmarkDao: BookmarkDao
    lateinit var collectionWithBooksDao: CollectionWithBooksDao

    @Before
    fun initialize() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        JodaTimeAndroid.init(context)

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        userDao = db.getUserDao()
        collectionDao = db.getCollectionDao()
        bookDao = db.getBookDao()
        bookmarkDao = db.getBookmarkDao()
        collectionWithBooksDao = db.getCollectionWithBooksDao()
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

/**
 * Tests of CollectionDao.
 */
@RunWith(AndroidJUnit4::class)
class CollectionDaoTest : DatabaseInstTest() {

    @Test
    @Throws(Exception::class)
    fun createAndRead() {
        val collection = Collection(0, "Fantasy", 0)

        val id = collectionDao.insert(collection)
        val readCollection = collectionDao.getCollection(id).getValueBlocking()
        assertThat("Two collection should be same", readCollection, equalTo(collection))
    }

    @Test
    @Throws(Exception::class)
    fun relationBetweenCollectionAndBooks() {
        val collection = Collection(0, "Fantasy", 0)
        val collectionId = collectionDao.insert(collection)

        val books = listOf(
            Book(Uri.parse("file:///~/book"), "Book#1", Uri.parse("file:///~/cover"), "", 0, DateTime.now()),
            Book(Uri.parse("file:///~/book2"), "Book#2", Uri.parse("file:///~/cover2"), "", 0, DateTime.now())
        )

        books.forEach {
            val bookId = bookDao.insert(it)
            collectionWithBooksDao.insert(CollectionBookCrossRef(collectionId, bookId))
        }

        val withBooks = collectionWithBooksDao.getCollectionWithBooks(collectionId).getValueBlocking()

        assertThat("CollectionWithBooks should not null", withBooks, notNullValue())
        assertThat("CollectionWithBooks should have two books", withBooks!!.books.size, equalTo(2))
    }

    @Test
    @Throws(Exception::class)
    fun cascadeBooksDelete() {
        val collection = Collection(0, "Fantasy", 0)
        val collectionId = collectionDao.insert(collection)

        val books = listOf(
            Book(Uri.parse("file:///~/book"), "Book#1", Uri.parse("file:///~/cover"), "", 0, DateTime.now()),
            Book(Uri.parse("file:///~/book2"), "Book#2", Uri.parse("file:///~/cover2"), "", 0, DateTime.now())
        )

        books.forEach {
            val bookId = bookDao.insert(it)
            collectionWithBooksDao.insert(CollectionBookCrossRef(collectionId, bookId))
        }

        val withBooks = collectionWithBooksDao.getCollectionWithBooks(collectionId).getValueBlocking()

        bookDao.delete(withBooks!!.books[0])
        assertThat("A book should be deleted", bookDao.getBooks().getValueBlocking()!!.size, equalTo(1))

        assertThat("Relation should have one item", collectionWithBooksDao.getRelations().getValueBlocking()!!.size, equalTo(1))

        val withBooks2 = collectionWithBooksDao.getCollectionWithBooks(collectionId).getValueBlocking()
        assertThat("CollectionWithBooks should have one books", withBooks2!!.books.size, equalTo(1))
    }

    @Test
    @Throws(Exception::class)
    fun cascadeCollectionDelete() {
        val collection = Collection(0, "Fantasy", 0)
        val collectionId = collectionDao.insert(collection)

        val books = listOf(
            Book(Uri.parse("file:///~/book"), "Book#1", Uri.parse("file:///~/cover"), "", 0, DateTime.now()),
            Book(Uri.parse("file:///~/book2"), "Book#2", Uri.parse("file:///~/cover2"), "", 0, DateTime.now())
        )

        books.forEach {
            val bookId = bookDao.insert(it)
            collectionWithBooksDao.insert(CollectionBookCrossRef(collectionId, bookId))
        }

        collection.collectionId = collectionId
        collectionDao.delete(collection)
        assertThat("A collection should be deleted", collectionDao.getCollections().getValueBlocking()!!.size, equalTo(0))

        assertThat("Relation should have no item", collectionWithBooksDao.getRelations().getValueBlocking()!!.size, equalTo(0))
    }
}

/**
 * Tests of BookDao.
 */
@RunWith(AndroidJUnit4::class)
class BookDaoTest : DatabaseInstTest() {

    @Test
    @Throws(Exception::class)
    fun createAndRead() {
        val book = Book(Uri.parse("file:///~/book"), "Book#1", Uri.parse("file:///~/cover"), "", 0, DateTime.now())
        val id = bookDao.insert(book)

        val readBook = bookDao.getBook(id).getValueBlocking()
        assertThat("Two books should be same", readBook, equalTo(book))
    }

    @Test
    @Throws(Exception::class)
    fun relationBetweenBookAndBookmarks() {
        val book = Book(Uri.parse("file:///~/book"), "Book#1", Uri.parse("file:///~/cover"), "", 0, DateTime.now())
        val id = bookDao.insert(book)

        val bookmarks = listOf(
            Bookmark(id, "Chapter 1", 100, BookmarkType.CUSTOM.name, DateTime.now()),
            Bookmark(id, "Chapter 2", 100, BookmarkType.CUSTOM.name, DateTime.now()),
            Bookmark(id, "Chapter 3", 100, BookmarkType.CUSTOM.name, DateTime.now())
        )

        bookmarks.forEach {
            bookmarkDao.insert(it)
        }

        val withBookmarks = bookDao.getBookWithBookmarks(id).getValueBlocking()

        assertThat("BookWithBookmarks should not null", withBookmarks, notNullValue())
        assertThat("BookWithBookmarks should have three bookmarks", withBookmarks!!.bookmarks.size, equalTo(3))
    }

    @Test
    @Throws(Exception::class)
    fun cascadeBookmarksDelete() {
        val book = Book(Uri.parse("file:///~/book"), "Book#1", Uri.parse("file:///~/cover"), "", 0, DateTime.now())
        val id = bookDao.insert(book)

        val bookmarks = listOf(
            Bookmark(id, "Chapter 1", 100, BookmarkType.CUSTOM.name, DateTime.now()),
            Bookmark(id, "Chapter 2", 100, BookmarkType.CUSTOM.name, DateTime.now()),
            Bookmark(id, "Chapter 3", 100, BookmarkType.CUSTOM.name, DateTime.now())
        )

        bookmarks.forEach {
            bookmarkDao.insert(it)
        }

        book.bookId = id
        bookDao.delete(book)
        val readBook = bookDao.getBook(id).getValueBlocking()
        assertThat("Book should deleted", readBook, nullValue())

        val readBookmarks = bookmarkDao.getBookmarks(id).getValueBlocking()
        assertThat("Size of bookmarks should be zero", readBookmarks!!.size, equalTo(0))
    }
}

/**
 * Blocking observer to test LiveData.
 */
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
