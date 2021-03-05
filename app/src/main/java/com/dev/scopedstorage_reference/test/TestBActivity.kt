package com.dev.scopedstorage_reference.test

import android.content.Intent
import android.os.Bundle
import com.dev.scopedstorage_reference.BaseActivity
import com.dev.scopedstorage_reference.databinding.ActivityTestBBinding

class TestBActivity: BaseActivity() {
    private lateinit var binding: ActivityTestBBinding
    val TAG = "#### Activity B ####"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding setting
        binding = ActivityTestBBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        binding.btnActivityC.setOnClickListener {
            startActivity(Intent(this, TestCActivity::class.java))
        }

        binding.btnRunCamera.setOnClickListener {
            runCamera()
        }

    }
    
}