package com.djacoronel.randombookrecommender.view

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.djacoronel.randombookrecommender.R
import com.djacoronel.randombookrecommender.model.Book
import com.djacoronel.randombookrecommender.network.BookService
import com.djacoronel.randombookrecommender.network.RetrofitHelper
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import java.util.Random


class MainActivity : AppCompatActivity() {

    private lateinit var bookService: BookService
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookService = RetrofitHelper().getBookService()
        setupBottomSheetBehavior()
    }

    private fun setupBottomSheetBehavior() {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        edit_text.inputType = InputType.TYPE_CLASS_TEXT
        edit_text.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                button.performClick()
                return@OnKeyListener true
            }
            false
        })

        keyword_text.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onStart() {
        super.onStart()

        val bookKeywordsObservable = createButtonClickObservable()
        compositeDisposable.add(
                bookKeywordsObservable
                        .doOnNext { progressBar.visibility = View.VISIBLE }
                        .subscribe { keywords ->
                            requestBooks(keywords)
                        })
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun createButtonClickObservable(): Observable<String> {
        return Observable.create { emitter ->
            button.setOnClickListener {
                emitter.onNext(edit_text.text.toString())
            }
            emitter.setCancellable {
                button.setOnClickListener(null)
            }
        }
    }

    private fun requestBooks(keywords: String) {
        compositeDisposable.add(
                bookService.queryBooks(keywords, Random().nextInt(50))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map { bookResponse -> bookResponse.items }
                        .subscribe({ books ->
                            progressBar.visibility = View.GONE
                            displayRandomBook(books)
                        }, { throwable ->
                            Log.d("ERROR", throwable.message)
                            toast("Server error, please try again :(")
                        })
        )
    }

    private fun displayRandomBook(books: List<Book>) {
        if (books.isNotEmpty()) {
            val randomNumber = Random().nextInt(books.size)
            val book = books[randomNumber]

            displayBookInfo(book)
            setSubtitleVisibility()
        } else {
            displayNoBookInfo()
        }
    }

    private fun displayBookInfo(book: Book) {
        Picasso.get().load(book.volumeInfo.imageLinks.thumbnail).into(book_cover)
        book_title.text = book.volumeInfo.title
        book_subtitle.text = book.volumeInfo.subtitle


        var authorYear = ""
        book.volumeInfo.authors?.let {
            authorYear = it.joinToString()
        }
        authorYear += " (${book.volumeInfo.publishedDate})"
        book_author_year.text = authorYear


        book.volumeInfo.description?.let {
            val description = it
            book_cover.setOnClickListener {
                alert(description).show()
            }
        }
    }

    private fun setSubtitleVisibility() {
        if (book_subtitle.text == "")
            book_subtitle.visibility = View.GONE
        else
            book_subtitle.visibility = View.VISIBLE
    }

    private fun displayNoBookInfo() {
        book_title.text = "No book to recommend for that keyword."
        book_subtitle.text = "Try other keywords."
        book_author_year.text = ":("
        book_cover.setImageResource(R.drawable.book)
    }
}
