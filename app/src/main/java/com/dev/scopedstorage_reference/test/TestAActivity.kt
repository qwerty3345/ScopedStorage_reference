package com.dev.scopedstorage_reference.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.dev.scopedstorage_reference.BaseActivity
import com.dev.scopedstorage_reference.databinding.ActivityTestABinding

class TestAActivity : BaseActivity() {
    private lateinit var binding: ActivityTestABinding
    val TAG = "#### Activity A ####"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding setting
        binding = ActivityTestABinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        binding.btnActivityB.setOnClickListener {
            startActivity(Intent(this, TestBActivity::class.java))
        }

    }

    fun activityLog(){
        Log.e(TAG, "activity A!!")
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}