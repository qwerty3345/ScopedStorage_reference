package com.dev.scopedstorage_reference

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.dev.scopedstorage_reference.test.TestCActivity
import com.dev.scopedstorage_reference.util.*
import com.dev.scopedstorage_reference.util.PermissionUtil.REQUEST_CAMERA_PERMISSION
import com.dev.scopedstorage_reference.util.PermissionUtil.REQUEST_READ_EXTERNAL_STORAGE
import com.dev.scopedstorage_reference.util.PermissionUtil.REQUEST_READ_WRITE_EXTERNAL_STORAGE
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 권한 체크 및 부여 처리,
 * onActivityResult 처리
 */
open class BaseActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val REQUEST_PICK_FILE_INTERNAL = 10
        const val REQUEST_PICK_FILE_EXTERNAL = 20
        const val REQUEST_CAMERA_PICTURE = 30
    }

    private val TAG = "###BaseActivity###"
    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        // Q 미만일 시, 처음에 저장소 permission 체크 및 권한 부여
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            PermissionUtil.checkStoragePermission(this)
        }
    }

    /**
     * 카메라 실행
     */
    fun runCamera() {
        if(PermissionUtil.checkCameraPermission(this)){
            val timeStamp = SimpleDateFormat("yyyyMMdd_hhMMss", Locale.KOREA).format(Date())
            val imageFileName = "IMG_" + timeStamp + "_"
            // 촬영 한 사진을 저장 할 경로
            val storageDir = FilePaths.getFilePath()
            val photoFile = File.createTempFile(imageFileName, ".jpg", storageDir).apply {
                currentPhotoPath = this.absolutePath
            Log.e(TAG, this.toString())
            }

            // 해당 구문들을 통해 카메라 촬영 시, currentPhotoPath 위치의 파일에 씀.
            val photoURI = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", photoFile)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, REQUEST_CAMERA_PICTURE)
        }
    }



    /**
     * 권한 요청 후, 결과값에 따른 처리
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                Log.e(TAG, "grantResults.size : ${grantResults.size} && grantResults[0] : ${grantResults[0]}")
                if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED) {
                    runCamera()
                } else {
                    AlertDialog.Builder(this).setTitle("카메라 권한 요청").setCancelable(false).setMessage("카메라를 실행하려면 권한을 허용해주세요!")
                        .setPositiveButton("설정") { _, _ -> PermissionUtil.checkCameraPermission(this) }
                        .setNegativeButton("취소") { _, _ -> }
                        .create().show()
                }
            }

            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED) {
                    Log.e(TAG, "storage read permission success")
                }
            }
            REQUEST_READ_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.size == 1 && grantResults[0] == PERMISSION_GRANTED) {
                    Log.e(TAG, "storage read & write permission success")
                }
            }
        }
    }


    /**
     * 1. 카메라 촬영 후 사진 저장 처리
     *
     * 2. SAF 에서 파일 선택 후, 돌아왔을 때 내외부 저장소 복사 처리
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // 사진 촬영 후 돌아왔을 때의 처리.
            REQUEST_CAMERA_PICTURE -> {

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true // 디코딩 시 메모리 할당 방지.
                }

                try {
                    /*FileInputStream(currentPhotoPath).use {   // use문 사용한 버전 -> 가독성이 좀 떨어지나?
                        BitmapFactory.decodeStream(it, null, options)
                    }*/

                    val ins = FileInputStream(currentPhotoPath)
                    BitmapFactory.decodeStream(ins, null, options)
                    ins.close()

                    val bitmap = BitmapFactory.decodeFile(currentPhotoPath)

                    val displayName = "${System.currentTimeMillis()}.jpg"
                    val compressFormat = Bitmap.CompressFormat.JPEG

                    Log.e(TAG, "카메라 촬영 후 처리 호출!")

                    // 외부 저장소에 사진 저장
                    val savedImageUri = StorageUtil.addBitmapToDCIM(this, bitmap, displayName, compressFormat)

                     if (this is MainActivity){
                        this.loadImage(savedImageUri)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // SAF로 파일 선택 후 돌아왔을 때의 처리.
            REQUEST_PICK_FILE_INTERNAL, REQUEST_PICK_FILE_EXTERNAL -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data
                    var fileName = ""

                    if (uri != null) {
                        contentResolver.query(uri, null, null, null, null)
                            ?.use { cursor ->
                                if (cursor.count > 0) {
                                    cursor.moveToFirst()
                                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                                    Log.e(TAG, "#2 fileName : $fileName, uri : $uri")
                                }
                                if (requestCode == REQUEST_PICK_FILE_EXTERNAL) {
                                    // 외부저장소 저장
                                    StorageUtil.saveFileFromUri(this, uri, fileName, true).let {
                                        if(this is MainActivity){
                                            this.loadImage(it)
                                        }
                                        Toast.makeText(this, "$it 에 저장함", Toast.LENGTH_LONG).show()
                                    }

                                } else {
                                    // 내부저장소 저장
                                    StorageUtil.saveFileFromUri(this, uri, fileName, false).let {
                                        if(this is MainActivity){
                                            this.loadImage(it)
                                        }
                                        Toast.makeText(this, "$it 에 저장함", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }


}