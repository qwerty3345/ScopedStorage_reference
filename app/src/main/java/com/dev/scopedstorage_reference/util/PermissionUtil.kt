package com.dev.scopedstorage_reference.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/** 권한을 체크하고, 부여하기 위한 유틸 */
object PermissionUtil {

    /** Request Codes */
    const val REQUEST_CAMERA_PERMISSION = 1000
    const val REQUEST_READ_EXTERNAL_STORAGE = 1001
    const val REQUEST_READ_WRITE_EXTERNAL_STORAGE = 1002
    const val REQUEST_FORE_LOCATION_PERMISSION = 1003
    const val REQUEST_BACK_LOCATION_PERMISSION = 1004
    const val REQUEST_CONTACTS_PERMISSION = 1005
    const val REQUEST_RECORD_AUDIO_PERMISSION = 1005

    /** Permissions */
    const val READ_EXTERNAL_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    const val WRITE_EXTERNAL_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    const val LOCATION_FOREGROUND_PERMISSION = android.Manifest.permission.ACCESS_FINE_LOCATION
    const val LOCATION_BACKGROUND_PERMISSION = android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    const val CONTACTS_READ_PERMISSION = android.Manifest.permission.READ_CONTACTS
    const val RECORD_AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO
    const val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED

    /**
     * 특정 permission 권한 체크 및 권한 요청
     *
     * @param activity 액티비티 컨텍스트
     * @param permission 체크 할 권한
     */
    fun checkPermission(activity: AppCompatActivity, permission: String): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED) {
            true
        } else {
            requestPermission(activity, permission)
            false
        }
    }

    /**
     * Camera 권한 체크 및 권한 요청
     *
     * @param activity 액티비티 컨텍스트
     */
    fun checkCameraPermission(activity: AppCompatActivity): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PERMISSION_GRANTED) {
            true
        } else {
            requestCameraPermission(activity)
            false
        }
    }

    /**
     * Foreground 위치 권한 체크 및 권한 요청
     *
     * @param activity 액티비티 컨텍스트
     */
    fun checkForeLocationPermission(activity: AppCompatActivity): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, LOCATION_FOREGROUND_PERMISSION) == PERMISSION_GRANTED) {
            true
        } else {
            requestForeLocationPermission(activity)
            false
        }
    }

    /**
     * Background 위치 권한 체크 및 권한 요청
     *
     * @param activity 액티비티 컨텍스트
     */
    fun checkBackLocationPermission(activity: AppCompatActivity): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, LOCATION_FOREGROUND_PERMISSION) == PERMISSION_GRANTED) {
            true
        } else {
            requestForeLocationPermission(activity)
            false
        }
    }

    /**
     * Storage 권한 체크 및 권한 요청
     *
     * @param activity 액티비티 컨텍스트
     */
    fun checkStoragePermission(activity: AppCompatActivity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return if (ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_PERMISSION) == PERMISSION_GRANTED) {
                true
            } else {
                requestStoragePermission(activity)
                false
            }
        } else {
            return if (ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_PERMISSION) == PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_PERMISSION) == PERMISSION_GRANTED
            ) {
                return true
            } else {
                requestStoragePermission(activity)
                false
            }
        }
    }

    /**
     * 주소록 권한 체크 및 권한 요청
     */
    fun checkContactsPermission(activity: AppCompatActivity): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, CONTACTS_READ_PERMISSION) == PERMISSION_GRANTED) {
            true
        } else {
            requestContactsPermission(activity)
            false
        }
    }

    /**
     * 음성 녹음 권한 체크 및 권한 요청
     */
    fun checkRecordAudioPermission(activity: AppCompatActivity): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, RECORD_AUDIO_PERMISSION) == PERMISSION_GRANTED) {
            true
        } else {
            requestRecordAudioPermission(activity)
            false
        }
    }



    /********************************* Permission 부여 *********************************/

    /** 특정 권한 요청 */
    private fun requestPermission(activity: AppCompatActivity, permission: String) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_READ_EXTERNAL_STORAGE)
    }

    /** storage 권한 요청 */
    private fun requestStoragePermission(activity: AppCompatActivity) {
        // Android Q 이상 : read 만 체크해서 받음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity, arrayOf(READ_EXTERNAL_PERMISSION), REQUEST_READ_EXTERNAL_STORAGE)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(READ_EXTERNAL_PERMISSION, WRITE_EXTERNAL_PERMISSION),
                REQUEST_READ_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    /** 저장공간 권한 (읽기, 쓰기) 둘 다 요청 _ Migration을 위해 */
    private fun requestStoragePermissionBoth(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, arrayOf(READ_EXTERNAL_PERMISSION, WRITE_EXTERNAL_PERMISSION), REQUEST_READ_EXTERNAL_STORAGE)
    }

    /** camera 권한 요청 */
    private fun requestCameraPermission(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity, arrayOf(CAMERA_PERMISSION), REQUEST_CAMERA_PERMISSION)
        }
    }

    /** Foreground 위치 권한 요청 */
    private fun requestForeLocationPermission(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, arrayOf(LOCATION_FOREGROUND_PERMISSION), REQUEST_FORE_LOCATION_PERMISSION)
    }

    /** Background 위치 권한 요청 */
    private fun requestBackLocatePermission(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, arrayOf(LOCATION_BACKGROUND_PERMISSION), REQUEST_BACK_LOCATION_PERMISSION)
    }

    /** 주소록 위치 권한 요청 */
    private fun requestContactsPermission(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, arrayOf(CONTACTS_READ_PERMISSION), REQUEST_CONTACTS_PERMISSION)
    }

    /** 녹음 기능 (마이크) 권한 요청 */
    private fun requestRecordAudioPermission(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, arrayOf(RECORD_AUDIO_PERMISSION), REQUEST_RECORD_AUDIO_PERMISSION)
    }
}
