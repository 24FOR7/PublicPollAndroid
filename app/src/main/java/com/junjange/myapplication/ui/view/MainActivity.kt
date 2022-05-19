package com.junjange.myapplication.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.junjange.myapplication.R
import com.junjange.myapplication.adapter.QuickVoteAdapter
import com.junjange.myapplication.databinding.ActivityMainBinding
import com.junjange.myapplication.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by lazy { ViewModelProvider(this, MainViewModel.Factory(application))[MainViewModel::class.java] }
    private lateinit var retrofitAdapter: QuickVoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        /**
         * drawer
         *
         * */
        setSupportActionBar(binding.mainToolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        setView()
        setObserver()

        // 투표 검색 페이지로 이동
        binding.searchBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))

        }

        // 핫 투표 페이지로 이동
        binding.hotPollsBtn.setOnClickListener {
            startActivity( Intent(this@MainActivity, HotPollsActivity::class.java))

        }

        // 모든 투표 페이지로 이동
        binding.allPollsBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PollsActivity::class.java))

        }

        // 투표 생성 페이지로 이동
        binding.newPollBtn.setOnClickListener {
            Toast.makeText(this, "투표 생성 페이지로 이동", Toast.LENGTH_SHORT).show()
//            startActivity(Intent(this@MainActivity, ::class.java))

        }

    }

    private fun setView(){
        retrofitAdapter =  QuickVoteAdapter().apply {
            setHasStableIds(true) // 리사이클러 뷰 업데이트 시 깜빡임 방지
        }
        binding.rvList.adapter = retrofitAdapter
    }

    private fun setObserver() {
        viewModel.retrofitTodoList.observe(this, {

            viewModel.retrofitTodoList.value?.let { it1 -> retrofitAdapter.setData(it1) }
        })

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{ // 메뉴 버튼
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)    // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }



}