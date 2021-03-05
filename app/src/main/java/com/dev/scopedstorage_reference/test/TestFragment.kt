package com.dev.scopedstorage_reference.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.scopedstorage_reference.BaseActivity
import com.dev.scopedstorage_reference.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    private lateinit var binding : FragmentTestBinding

    /*interface FragmentInterface{
        fun fragmentTest()
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 인터페이스로 구현한 방식. 복잡하지만, 이렇게 하면 여러 액티비티에서 Interface를 구현하기만 하면 모두 사용할 수 있다.
//        (activity as FragmentInterface).fragmentTest()

        binding.btnActivityB.setOnClickListener {
            // 액티비티 형변환 후 runCamera 실행 가능! _ A 액티비티가 BaseActivity 를 상속받았으니 가능.
            (activity as BaseActivity).runCamera()
        }
    }

}