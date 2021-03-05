package com.dev.scopedstorage_reference

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.dev.scopedstorage_reference.databinding.ActivityAlbumBinding
import com.dev.scopedstorage_reference.util.AlbumAdapter
import kotlin.concurrent.thread

/**
 * 앨범에 있는 사진들을 보여주는 Activity
 */
class AlbumActivity : AppCompatActivity() {
    lateinit var binding: ActivityAlbumBinding
    val imageList = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlbumBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        binding.rvAlbum.let {
            // 해당 뷰가 그려지기 직전에 실행될 동작 등록
            it.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    it.viewTreeObserver.removeOnPreDrawListener(this)   // 해제
                    val columns = 3
                    val imageSize = it.width / columns
                    val adapter = AlbumAdapter(this@AlbumActivity, imageList, imageSize)
                    it.layoutManager = GridLayoutManager(this@AlbumActivity, columns)
                    it.adapter = adapter
                    loadImages(adapter)
                    return true
                }
            })
        }
    }

    private fun loadImages(adapter: AlbumAdapter) {
        // 얘를 thread로 감싸야 일단 Activity로 이동하고, 로딩이 시작됨.
        // 아니면 로딩 다 된 후에야 Activity로 이동함.
        thread {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                "${MediaStore.MediaColumns.DATE_ADDED} desc"
            )?.use { cursor ->  // use 를 사용하면 close()인 자원회수를 자동으로 해줌.
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    // 이미지 Uri 추가
                    imageList.add(uri)
                }
            }
        }

        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }
}
