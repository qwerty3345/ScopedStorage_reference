package com.dev.scopedstorage_reference

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.dev.scopedstorage_reference.databinding.ActivityMainBinding
import com.dev.scopedstorage_reference.test.TestAActivity
import com.dev.scopedstorage_reference.util.FilePaths
import com.dev.scopedstorage_reference.util.PermissionUtil
import com.dev.scopedstorage_reference.util.StorageUtil
import java.io.File
import java.io.FileInputStream
import java.util.*

class MainActivity : BaseActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var mContext: Context
    val TAG = "####MainActivity####"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding setting
        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)
        mContext = this


        /**
         * 앨범 보기 (RecyclerView 구현 Activity로 이동)
         */
        binding.btnBrowseAlbum.setOnClickListener {
            if (PermissionUtil.checkStoragePermission(this)) {
                val intent = Intent(this, AlbumActivity::class.java)
                startActivity(intent)
            }
        }

        /**
         * 이미지 저장 (앱 내부저장소)
         */
        binding.btnAddImageToInternal.setOnClickListener {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.add_internal_image)
            val displayName = "${System.currentTimeMillis()}.jpg"
            val compressFormat = Bitmap.CompressFormat.JPEG

            val savedFile = StorageUtil.addBitmapToInternal(bitmap, displayName, compressFormat)
            loadImage(savedFile.toString())
        }

        /**
         * 이미지 저장 (앱 외부저장소)
         */
        binding.btnAddImageToExternal.setOnClickListener {
            // 랜덤으로 이미지 가져오도록, 별의미 없음. 그냥 테스트 하려고..
            val rand = Random().nextInt(5) + 1
            val imageRes = when (rand) {
                1 -> R.drawable.add_external_image_1
                2 -> R.drawable.add_external_image_2
                3 -> R.drawable.add_external_image_3
                4 -> R.drawable.add_external_image_4
                else -> R.drawable.add_external_image_5
            }

            val bitmap = BitmapFactory.decodeResource(resources, imageRes)
            val displayName = "${System.currentTimeMillis()}.jpg"
            val compressFormat = Bitmap.CompressFormat.JPEG

            val savedFile = StorageUtil.addBitmapToExternal(bitmap, displayName, compressFormat)
            loadImage(savedFile.toString())
        }

        /**
         * 이미지 저장 (DCIM 폴더)
         */
        binding.btnAddImageToDCIM.setOnClickListener {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.add_dcim_image)
            val displayName = "${System.currentTimeMillis()}.jpg"
//            val mimeType = "image/jpeg"
            val compressFormat = Bitmap.CompressFormat.JPEG
            val savedFile = StorageUtil.addBitmapToDCIM(this, bitmap!!, displayName, compressFormat)
            loadImage(savedFile.toString())

        }

        /**
         * 카메라 사진 촬영 후 외부 저장소에 저장
         *
         * (이후 onActivityResult에서 처리)
         */
        binding.btnCamera.setOnClickListener {
            runCamera()
        }

        /**
         * 파일 다운로드 (MediaStore의 Download 폴더에 저장)
         *
         * 텍스트 파일, 이미지 파일
         */
        binding.btnDownloadFile.setOnClickListener {
            val fileUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/04/sample-text-file.txt"
            val fileName = "sample.txt"
            StorageUtil.downloadFileFromURL(this, fileUrl, fileName)

            val imageFileUrl = "https://www.learningcontainer.com/bfd_download/small-jpeg-file-for-testing-download/"
            val imageFileName = "sample.jpg"
            StorageUtil.downloadFileFromURL(this, imageFileUrl, imageFileName)
        }

        /**
         * 파일 선택해서 저장소에 복사 (내부저장소, SAF)
         *
         * (이후 onActivityResult에서 처리)
         */
        binding.btnPickFileInternal.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*" // 이미지로 제한하려면 image/*

            val initialUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:DCIM")   //"primary:DCIM" 가능
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            startActivityForResult(intent, REQUEST_PICK_FILE_INTERNAL)
        }

        /**
         * 파일 선택해서 저장소에 복사 (외부저장소, SAF)
         *
         * (이후 onActivityResult에서 처리)
         */
        binding.btnPickFileExternal.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, REQUEST_PICK_FILE_EXTERNAL)
        }

        /**
         * 특정 확장자의 파일들 검색 해 옴
         */
        binding.btnSearchFile.setOnClickListener {
//            val folder = getExternalFilesDir("")
//            val extensionArr = arrayOf(".txt", ".jpg")
//            val searchedFiles = StorageUtil.filterByExtension(folder!!, extensionArr)

            val folder = Environment.getExternalStorageDirectory()
            val extensionArr = arrayOf(".m4a")
            val searchedFiles = StorageUtil.filterByExtension(folder!!, extensionArr)



            Log.e(TAG, "폴더 검색 \n 위치: $folder, 확장자: $extensionArr, 검색된 파일 수: ${searchedFiles.size}")
            Toast.makeText(
                this, "폴더 검색 \n" +
                        " 위치: $folder, 확장자: $extensionArr, 검색된 파일 수: ${searchedFiles.size}", Toast.LENGTH_LONG
            ).show()

            val ext = "jpg"
            val searchUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI    // 검색 할 위치
            val mediaFiles = StorageUtil.filterByMediaStore(this, searchUri, ext)
            Log.e(TAG, "미디어스토어 검색 \n 위치 : $searchUri, 확장자: $ext, 검색된 파일 수: ${mediaFiles.size}")
        }

        /**
         * 앱 외부 저장소에 저장한 파일 이름 변경 테스트 (잘 됨.)
         */
        binding.btnChangeFileName.setOnClickListener {
            val file = File(FilePaths.getFilePath(), "test1.jpg")
            if (file.exists()) {
                file.renameTo(File(FilePaths.getFilePath(), "test2.jpg"))

                Log.e(TAG, "test1.jpg -> test2.jpg")
                Toast.makeText(this, "test1.jpg -> test2.jpg", Toast.LENGTH_SHORT).show()
            } else {
                val file2 = File(FilePaths.getFilePath(), "test2.jpg")
                file2.renameTo(File(FilePaths.getFilePath(), "test1.jpg"))

                Log.e(TAG, "test2.jpg -> test1.jpg")
                Toast.makeText(this, "test2.jpg -> test1.jpg", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRecordingFile.setOnClickListener {
            val intent = Intent(this, RecorderActivity::class.java)
            startActivity(intent)
        }

        binding.btnTestActivity.setOnClickListener {
            startActivity(Intent(this, TestAActivity::class.java))
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    fun loadImage(path: String) {
        Glide.with(this).load(path).into(binding.ivMainImage)
    }

    fun loadImage(path: Uri?) {
        Glide.with(this).load(path).into(binding.ivMainImage)
    }

}