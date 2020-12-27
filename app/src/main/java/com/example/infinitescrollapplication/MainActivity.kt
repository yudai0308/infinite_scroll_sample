package com.example.infinitescrollapplication

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

    /**
     * API から取得するデータセットを想定したプロパティ。
     */
    private val dataSet = mutableListOf<String>()

    /**
     * API に問い合わせ中は true になる。
     */
    private var nowLoading = false

    private lateinit var myAdapter: MyAdapter

    private val handler = Handler()

    private val progressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }

    init {
        // 仮想データセットを生成。
        for (i in 0..99) { dataSet.add("Number $i") }
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

    /**
     * API でリストデータを取得することを想定したメソッド。
     */
    private suspend fun fetch(index: Int): List<String> {
        // API 問い合わせの待ち時間を仮想的に作る。
        handler.post { progressBar.visibility = View.VISIBLE }
        delay(3000)
        handler.post { progressBar.visibility = View.INVISIBLE }

        return when (index) {
            in 0..90 -> dataSet.slice(index..index + 9)
            in 91..99 -> dataSet.slice(index..99)
            else -> listOf()
        }
    }

    /**
     * API でリストデータを取得して画面に反映することを想定したメソッド。
     */
    private suspend fun fetchAndUpdate(index: Int) {
        val fetchedData = withContext(Dispatchers.Default) {
            fetch(index)
        }

        // 取得したデータを画面に反映。
        if (fetchedData.isNotEmpty()) {
            handler.post { myAdapter.add(fetchedData) }
        }
        // 問い合わせが完了したら false に戻す。
        nowLoading = false
    }

    /**
     * リストの下端までスクロールしたタイミングで発火するリスナー。
     */
    inner class InfiniteScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // アダプターが保持しているアイテムの合計
            val itemCount = myAdapter.itemCount
            // 画面に表示されているアイテム数
            val childCount = recyclerView.childCount
            val manager = recyclerView.layoutManager as LinearLayoutManager
            // 画面に表示されている一番上のアイテムの位置
            val firstPosition = manager.findFirstVisibleItemPosition()

            // 何度もリクエストしないようにロード中は何もしない。
            if (nowLoading) {
                return
            }

            // 以下の条件に当てはまれば一番下までスクロールされたと判断できる。
            if (itemCount == childCount + firstPosition) {
                // API 問い合わせ中は true となる。
                nowLoading = true
                GlobalScope.launch {
                    fetchAndUpdate(myAdapter.itemCount)
                }
            }
        }
    }
}
