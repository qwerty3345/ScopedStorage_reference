package com.dev.scopedstorage_reference.callback

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class TestMain : TestCallback, AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TestUtil.setCallback(this)  // 액티비티 자체를 콜백으로 구현하고, 넣어줌.
        TestUtil.setCallback(object : TestCallback{
            override fun success(result: String) {
                Log.e("#####another!######", result)
            }

            override fun fail(result: String) {
                TODO("Not yet implemented")
            }

        })
        TestUtil.doCallback()
    }


    override fun onDestroy() {
        super.onDestroy()
//        TestUtil.removeCallback()
    }


    override fun success(result: String) {
        Log.e("#####success######", result)
    }

    override fun fail(result: String) {
        Log.e("#####fail######", result)
    }

}