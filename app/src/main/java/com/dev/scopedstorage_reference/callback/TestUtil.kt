package com.dev.scopedstorage_reference.callback

object TestUtil {
    private val callbacks = mutableListOf<TestCallback>()
    private var resultBoolean = true

    fun setCallback(callback: TestCallback){
        callbacks.add(callback)
    }

    fun removeCallback(callback: TestCallback){
        callbacks.remove(callback)
    }

    fun doCallback(){
        for(callback in callbacks){
            if(resultBoolean){
                callback.success("성공")
            } else {
                callback.fail("실패")
            }
        }
    }
}