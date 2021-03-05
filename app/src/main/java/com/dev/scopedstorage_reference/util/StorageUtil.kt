package com.dev.scopedstorage_reference.util

import android.app.DownloadManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import java.io.*
import java.net.URLConnection

/**
 * 저장공간 유틸
 *
 * Scoped Storage 호환 및 버전별 분기처리
 */
object StorageUtil {
    private const val TAG = "####Storage Util####"

    /**
     * 특정 폴더와 그 내부 폴더들을 탐색 해, 특정 확장자명인 파일들을 불러오는 함수
     *
     * @param folder 탐색 할 폴더 (하위 폴더들 포함)
     * @param extensions 검색 할 확장자들의 배열
     * @return 불러온 파일들 ArrayList
     */
    fun filterByExtension(folder: File, extensions: Array<String>): ArrayList<String> {
        val result = arrayListOf<String>()
        val files = folder.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (file.isDirectory) {
                    result.addAll(filterByExtension(file, extensions))  // 재귀함수
                } else {
                    Log.e("#####", file.name)
                    try {
                        if (extensions.contains(file.name.substring(file.name.lastIndexOf(".")))) {
                            result.add(file.toString())
                        }
                    }catch (e: java.lang.Exception){
                        continue
                    }
                }
            }
        }
        return result
    }

    /**
     * MediaStore를 이용 해 특정 확장자명의 Mime Type인 파일들을 불러오는 함수
     *
     * @param context Context
     * @param searchUri 검색 할 위치 Uri (MediaStore 위치형식)
     * @param extension 불러올 파일들의 확장자
     * @return 불러온 파일들 ArrayList
     */
    fun filterByMediaStore(context: Context, searchUri: Uri, extension: String): ArrayList<Uri> {
        val result = arrayListOf<Uri>()

        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val selectionArgs: Array<String?> = arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension))
        Log.e(TAG, "selection: $selection, selectionArgs: ${selectionArgs[0]}")
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} desc"

        context.contentResolver.query(
            searchUri, // 원하는 데이터를 가져오기 위한 정해진 주소 (content://scheme 방식)
            null, // null이면 모든 열 반환
            selection,      // WHERE 절에 해당하는 내용
            selectionArgs,  // selection에서 ?로 표시된 곳에 들어갈 데이터 (argument)
            sortOrder       // 정렬을 위한 ORDER BY 구문
        )
            ?.use { cursor ->  // use 를 사용하면 close()인 자원회수를 자동으로 해줌.
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val uri = ContentUris.withAppendedId(searchUri, id)
                    result.add(uri)
                }
            }
        return result
    }


    /**
     * Bitmap 이미지를 앱 내부저장소에 저장
     *
     * 저장경로 : /data/user/0/패키지명/files
     *
     * @param bitmap 저장할 비트맵 파일
     * @param displayName 저장할 파일명
     * @param compressFormat 압축 타입
     * @return 저장한 파일 (File 형식)
     */
    fun addBitmapToInternal(bitmap: Bitmap, displayName: String, compressFormat: Bitmap.CompressFormat): File? {
        try {
            val path = File(FilePaths.internalPath, displayName)
            FileOutputStream(path).use { fos ->
                // bitmap을 fos 위치에 저장
                bitmap.compress(compressFormat, 100, fos)
            }
            Log.e(TAG, "파일을 $path 위치에 저장함")

            return path
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Bitmap 이미지를 앱 외부저장소에 저장
     *
     * @param bitmap 저장할 비트맵 파일
     * @param displayName 저장할 파일명
     * @param compressFormat 압축 타입
     * @return 저장한 파일 (File 형식)
     */
    fun addBitmapToExternal(bitmap: Bitmap, displayName: String, compressFormat: Bitmap.CompressFormat): File? {
        try {
            val path = File(FilePaths.getFilePath(), displayName)

            FileOutputStream(path).use { fos ->
                // bitmap을 fos 위치에 저장
                bitmap.compress(compressFormat, 100, fos)
            }

            Log.e(TAG, "파일을 $path 위치에 저장함")

            return path
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Bitmap 이미지를 앨범에 저장 (DCIM 폴더)
     *
     * 저장경로 : /storage/self/primary/DCIM (Q이상)
     *
     * @param bitmap 저장할 비트맵 파일
     * @param displayName 저장할 파일명
     * @param compressFormat 압축 타입
     * @return 저장한 파일의 Uri 정보
     */
    fun addBitmapToDCIM(context: Context, bitmap: Bitmap, displayName: String, compressFormat: Bitmap.CompressFormat): Uri? {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)

        // Android Q 이상 - DCIM 공유 디렉토리로 지정,  경로: /storage/self/primary/DCIM
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        } else {
            // Android Q 미만 - 외부 저장소 DCIM 폴더로 지정,  경로: /storage/sdcard/DCIM
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/DCIM/$displayName")
        }

        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?.let { uri ->
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(compressFormat, 100, outputStream)
                    outputStream.close()
                    Log.e(TAG, "사진을 $uri 위치에 저장함.")
                }
                return uri
            }
        return null
    }

    /**
     * url 정보를 바탕으로 파일을 다운로드
     *
     * 파일 타입에 따라 다운로드 위치 지정 (현재는 이미지, 다운로드만 구분)
     *
     * @param context 컨텍스트 정보
     * @param url 파일 다운로드 url
     * @param fileName 파일 저장 이름
     * @return 다운로드 한 파일의 path String
     */
    fun downloadFileFromURL(context: Context, url: String, fileName: String): String {
        val direction = if (isImageFile(fileName)) {
            Environment.DIRECTORY_PICTURES
        } else {
            Environment.DIRECTORY_DOWNLOADS
        }

        // DownloadManager도 내부적으로 MediaStore를 호출해서 처리
        val downloadManager = context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        try {
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
                .setTitle(fileName)
                .setDescription("Downloading")
                .setAllowedOverRoaming(true)    // 로밍일 때도 다운로드
                .setDestinationInExternalPublicDir(direction, fileName)
            downloadManager.enqueue(request)
            Log.e(TAG, "파일 다운로드 성공: 위치-$direction, 파일명-$fileName")
            Toast.makeText(context, "파일 다운로드 성공: 위치-$direction, 파일명-$fileName", Toast.LENGTH_LONG).show()
//            Log.e(TAG, URLConnection.guessContentTypeFromName(fileName))
            return "$direction/$fileName"
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    /**
     * 이미지 파일인지 mimeType으로 확인
     *
     * @param filePath 파일 위치 (or 파일 이름)
     * @return 이미지 파일 여부 Boolean
     */
    private fun isImageFile(filePath: String): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(filePath)
        return mimeType.startsWith("image")
                && BitmapFactory.decodeFile(filePath) != null
    }

    /**
     * uri에서 파일을 받아와 저장하는 메서드
     *
     * @param uri 파일 Uri
     * @param fileName 저장 할 파일명
     * @param isExternal true이면 외부저장소 저장, false면 내부 저장소 저장
     * @return 저장한 파일의 path String
     */
    fun saveFileFromUri(context: Context, uri: Uri, fileName: String, isExternal: Boolean): String {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempDir: File? = if (isExternal) {
                FilePaths.getFilePath()
            } else {
                FilePaths.internalPath
            }
            Log.e(TAG, "$tempDir")
            if (inputStream != null && tempDir != null) {
                val file = File("$tempDir/$fileName")
                val fos = FileOutputStream(file)
                val bis = BufferedInputStream(inputStream)  // is를 받아 생성
                val bos = BufferedOutputStream(fos) // fos를 받아 생성
                val byteArray = ByteArray(1024)
                var bytes = bis.read(byteArray)
                while (bytes > 0) {
                    bos.write(byteArray, 0, bytes)
                    bos.flush()
                    bytes = bis.read(byteArray)
                }
                bos.close()
                fos.close()
                Log.e(TAG, "파일을 $tempDir 위치에 저장함")

                return "$tempDir/$fileName"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
