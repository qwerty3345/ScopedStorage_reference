package com.dev.scopedstorage_reference.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

/**
 * 폴더 마이그레이션을 위한 유틸
 *
 * 폴더 복사, 폴더 삭제 기능 구현 (재귀함수 구조)
 */
object MigrationUtil {

    /**
     * 폴더 복사 (하위 폴더, 하위 파일들 포함)
     *
     * @param sourceDir 복사할 폴더 위치
     * @param destDir 붙여넣을 폴더 위치
     */
    fun copyDirectory(sourceDir: File, destDir: File) {
        val items = sourceDir.listFiles()   // 이전폴더의 파일들
        if (!items.isNullOrEmpty()) {
            for (item in items) {
                if (item.isDirectory) {
                    // item이 폴더이면 대상폴더에 하위 폴더 생성
                    val newDir = File(destDir, item.name)
                    Log.e("###Copy Directory###", "폴더 생성: " + newDir.absolutePath)
                    newDir.mkdir()

                    // 재귀함수 구조 _ 새로운 대상 폴더 안의 파일들 복사 처리
                    copyDirectory(item, newDir)
                } else {
                    // item이 파일이면 복사
                    val destFile = File(destDir, item.name)
                    copySingleFile(item, destFile)
                }
            }
        }
    }

    /**
     * 파일 복사
     *
     * copyDirectory에서 사용
     *
     * @param sourceFile 복사할 파일 위치
     * @param destFile 붙여넣을 파일 위치
     */
    private fun copySingleFile(sourceFile: File, destFile: File) {
        Log.e("###Copy Single File###", "파일 복사 _ FROM: " + sourceFile.absolutePath + " TO: " + destFile.absolutePath)
        if (!destFile.exists()) {
            destFile.createNewFile()
        }
        var sourceChannel: FileChannel? = null
        var destChannel: FileChannel? = null

        try {
            sourceChannel = FileInputStream(sourceFile).channel
            destChannel = FileOutputStream(destFile).channel
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel)
        } finally {
            sourceChannel?.close()
            destChannel?.close()
        }
    }

    /**
     * 폴더 삭제 (하위 폴더, 하위 파일들 포함)
     *
     * @param folder 삭제할 폴더 위치
     * @return 삭제 완료 여부
     */
    fun deleteDirectory(folder: File): Boolean {
        val time = System.currentTimeMillis()
        if (folder.exists()) {
            folder.listFiles()?.forEach {
                if (it.isDirectory) {
                    deleteDirectory(it)
                } else {
                    it.delete()
                }
            }
            val isDeleted = folder.delete()
            Log.e("###삭제###", "걸린시간 : ${(System.currentTimeMillis() - time)}, folder: $folder")
            return isDeleted
        } else {
            return false
        }
    }
}