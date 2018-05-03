package com.djacoronel.randombookrecommender

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import org.jetbrains.anko.alert
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var bookService: BookService
    private var compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookService = RetrofitHelper().getBookService()

        button.setOnClickListener { requestBooks() }

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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun requestBooks() {
        val keywords = edit_text.text.toString()

        compositeDisposable.add(
                bookService.queryBooks(keywords, Random().nextInt(50))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map { bookResponse -> bookResponse.items }
                        .subscribe({ books ->
                            displayBookInfo(books)
                        }, { throwable ->
                            Log.d("ERROR", throwable.message)
                        })
        )
    }

    private fun displayBookInfo(books: List<Book>) {
        if (books.isNotEmpty()) {
            val randomNumber = Random().nextInt(books.size)
            val item = books[randomNumber]

            Picasso.get().load(item.volumeInfo.imageLinks.thumbnail).into(book_cover)
            book_title.text = item.volumeInfo.title
            book_subtitle.text = item.volumeInfo.subtitle

            var authorYear = ""
            item.volumeInfo.authors?.let {
                authorYear = it.joinToString()
            }
            authorYear += " (${item.volumeInfo.publishedDate})"
            book_author_year.text = authorYear

            if (book_subtitle.text == "")
                book_subtitle.visibility = View.GONE
            else
                book_subtitle.visibility = View.VISIBLE

            book_cover.setOnClickListener {
                alert(item.volumeInfo.description!!).show()
            }
        } else {
            book_title.text = "No book to recommend for that keyword."
            book_subtitle.text = "Try other keywords."
            book_author_year.text = ":("
            book_cover.setImageResource(R.drawable.book)
        }
    }
}
