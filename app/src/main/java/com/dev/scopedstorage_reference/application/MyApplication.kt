package com.dev.scopedstorage_reference.application

import android.app.Application

/**
 * Application class
 *
 * Usage: FilePath Object
 */
class MyApplication : Application() {
    companion object{
        lateinit var instance : MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}