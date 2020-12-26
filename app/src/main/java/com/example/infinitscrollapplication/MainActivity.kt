package com.example.infinitscrollapplication

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val dataSet = mutableListOf<String>()

    private var nowLoading = false

    private lateinit var myAdapter: MyAdapter

    private val handler = Handler()

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }

    init {
        for (i in 0..100) { dataSet.add("Number $i") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listData = runBlocking { fetch(0) }
        myAdapter = MyAdapter(listData as MutableList<String>)
        findViewById<RecyclerView>(R.id.recycler_view).also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = myAdapter
            it.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            it.addOnScrollListener(InfiniteScrollListener())
        }
    }

    private suspend fun fetch(index: Int): List<String> {
        handler.post { progressBar.visibility = View.VISIBLE }
        delay(2000)
        handler.post { progressBar.visibility = View.INVISIBLE }

        return when (index) {
            in 0..90 -> dataSet.slice(index..index + 9)
            in 91..99 -> dataSet.slice(index..99 - index)
            else -> listOf()
        }
    }

    private suspend fun fetchAndUpdate(index: Int) {
        val fetchedData = withContext(Dispatchers.Default) {
            fetch(index)
        }

        handler.post { myAdapter.add(fetchedData) }
        nowLoading = false
    }

    inner class InfiniteScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val totalCount = recyclerView.adapter?.itemCount ?: 0
            val childCount = recyclerView.childCount
            val manager = recyclerView.layoutManager as LinearLayoutManager
            val firstPosition = manager.findFirstVisibleItemPosition()

            // 何度もリクエストしないようにロード中は何もしない。
            if (nowLoading) {
                return
            }

            if (totalCount == childCount + firstPosition) {
                nowLoading = true
                GlobalScope.launch {
                    fetchAndUpdate(myAdapter.itemCount)
                }
            }
        }
    }
}
