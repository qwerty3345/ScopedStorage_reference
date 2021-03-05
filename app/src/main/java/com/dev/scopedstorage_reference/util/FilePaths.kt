package com.dev.scopedstorage_reference.util

import android.os.Build
import android.os.Environment
import android.util.Log
import com.dev.scopedstorage_reference.application.MyApplication
import java.io.File

object FilePaths {
    private const val legacyFolderName = "지구시민톡"

    /** 내부 저장소 */
    val internalPath: File = MyApplication.instance.applicationContext.filesDir
    val newFilePath = MyApplication.instance.applicationContext.getExternalFilesDir("")
    val legecyFilePath = File(Environment.getExternalStorageDirectory().path + "/지구시민톡")


    /**
     * 버전에 따른 기본 외부 저장소 위치 반환
     */
    fun getFilePath(): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MyApplication.instance.applicationContext.getExternalFilesDir("")
        } else {
            val path = File(Environment.getExternalStorageDirectory().path + "/$legacyFolderName")
            if (!path.exists()) {
                path.mkdir()
            }
            path
        }
    }

    /**
     * 버전에 따른 기본 외부 저장소 내의 폴더 반환
     *
     * @param folder 기본 외부 저장소 내의 폴더 이름
     */
    fun getFilePath(folder: String): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MyApplication.instance.applicationContext.getExternalFilesDir(folder)
        } else {
            val path = File(Environment.getExternalStorageDirectory().path + "/$legacyFolderName/$folder")
            if (!path.exists()) {
                path.mkdir()
            }
            path
        }
    }

}