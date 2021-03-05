package com.dev.scopedstorage_reference.test

import android.os.Bundle
import com.dev.scopedstorage_reference.BaseActivity
import com.dev.scopedstorage_reference.databinding.ActivityTestCBinding

class TestCActivity : BaseActivity() {
    private lateinit var binding: ActivityTestCBinding
    val TAG = "#### Activity C ####"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewBinding setting
        binding = ActivityTestCBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        binding.btnRunCamera.setOnClickListener {
            runCamera()
        }
    }

}