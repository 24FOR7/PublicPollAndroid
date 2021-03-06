package com.junjange.myapplication.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.junjange.myapplication.R
import com.junjange.myapplication.adapter.CommentsAdapter
import com.junjange.myapplication.adapter.NormalVoteAdapter
import com.junjange.myapplication.adapter.PhotoVoteAdapter
import com.junjange.myapplication.data.ItemComponent
import com.junjange.myapplication.data.StatsItem
import com.junjange.myapplication.databinding.ActivityVoteBinding
import com.junjange.myapplication.ui.viewmodel.VoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class VoteActivity : AppCompatActivity(), NormalVoteAdapter.ItemClickListener, PhotoVoteAdapter.ItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    private val binding by lazy { ActivityVoteBinding.inflate(layoutInflater) }
    private val viewModel by lazy { ViewModelProvider(this, VoteViewModel.Factory(application))[VoteViewModel::class.java] }
    private lateinit var photoVoteAdapter: PhotoVoteAdapter
    private lateinit var normalVoteAdapter: NormalVoteAdapter
    private lateinit var commentsAdapter: CommentsAdapter
    private var checkVote = arrayListOf<Int>()
    private var canReVote = false
    private var canComment = true
    private var voteState: Boolean = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /**
         * drawer
         *
         * */
        setSupportActionBar(binding.mainToolbar) // ????????? ??????????????? ????????? ??????
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // ???????????? ?????? ??? ?????? ?????????
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze2_24) // ????????? ????????? ??????
        supportActionBar?.setDisplayShowTitleEnabled(false) // ????????? ????????? ????????????
        binding.mainNavigationView.setNavigationItemSelectedListener(this) //navigation ?????????

        // ????????? ??????
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        // ????????? ?????????
        binding.viewModel = viewModel
        binding.lifecycleOwner = this




        val id = intent.getSerializableExtra("id") as Int
        val presentImagePath = intent.getSerializableExtra("presentImagePath")
        voteState = intent.getBooleanExtra("voteState", false)

        viewModel.getCommentsRetrofit(id)
        viewModel.getViewPollsRetrofit(id)

        myPollsSetObserver(id)
        commentSetView()
        commentSetObserver()

        // ???????????? txt/card ?????????
        val hashtagTxtList = mutableListOf(binding.hashtagTxt1, binding.hashtagTxt2, binding.hashtagTxt3, binding.hashtagTxt4, binding.hashtagTxt5)
        val hashtagCardList = mutableListOf(binding.hashtagCard1, binding.hashtagCard2, binding.hashtagCard3, binding.hashtagCard4, binding.hashtagCard5)


        // ??????????????? ?????? ????????????/???????????? ?????????????????? ??????
        if (presentImagePath == null){
            normalSetView()
            normalSetObserver(hashtagTxtList, hashtagCardList )

        }else{
            photoSetView()
            photoSetObserver(hashtagTxtList, hashtagCardList)

        }

        // Vote Button
        binding.voteBtn.setOnClickListener {

            // ????????? ?????????
            if(checkVote.size > 0){

                // voteState??? ????????? ballot api/ revote api ??????
                if (voteState) viewModel.postReVoteRetrofit(id, checkVote) else viewModel.postBallotRetrofit(id, checkVote)

                // VoteActivity ?????? ??????
                val intent: Intent =  Intent(this@VoteActivity, VoteActivity::class.java).apply {
                    // ????????? ??????
                    putExtra("id", id)
                    putExtra("presentImagePath", presentImagePath)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                this.startActivity(intent)


            }else{
                Toast.makeText(this,"????????? ??????????????????.",Toast.LENGTH_SHORT).show()
            }
        }

        // ReVote Button
        binding.reVoteBtn.setOnClickListener {

            // VoteActivity ?????? ??????
            val intent: Intent = Intent(this@VoteActivity, VoteActivity::class.java).apply {
                // ????????? ??????
                putExtra("id", id)
                putExtra("presentImagePath", presentImagePath)
                putExtra("voteState", true) // revote?????? ???????????? ??????
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            }
            this.startActivity(intent)
        }

        // ?????? ??????
        binding.statisticsBtn.setOnClickListener {

            // VoteActivity ?????? ??????
            val intent: Intent =  Intent(this@VoteActivity, StatisticsActivity::class.java).apply {
                // ????????? ??????
                putExtra("id", id)
//                putExtra("presentImagePath", presentImagePath)
//                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            this.startActivity(intent)
        }


        // ?????? ??????
        binding.deleteBtn.setOnClickListener {

            onClickAllDeleteDialogButton(id)

        }


        // ????????? ??????
        binding.etCommentEnter.setOnKeyListener { _, keyCode, event ->

            if ((event.action== KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // ????????? ?????? ??? ?????? ?????? ???

                viewModel.postCommentRetrofit(id, binding.etCommentField.text.toString())
                binding.etCommentField.clearFocus()
                binding.etCommentField.text.clear()
                imm.hideSoftInputFromWindow(binding.etCommentField.windowToken, 0)
                true

            } else {

                false

            }
        }

        // ????????? ??????
        binding.etCommentEnter.setOnClickListener {
            viewModel.postCommentRetrofit(id, binding.etCommentField.text.toString())
            binding.etCommentField.clearFocus()
            binding.etCommentField.text.clear()
            imm.hideSoftInputFromWindow(binding.etCommentField.windowToken, 0)
        }

    }


    private fun normalSetView(){

        normalVoteAdapter =  NormalVoteAdapter(this, voteState).apply {
            setHasStableIds(true) // ??????????????? ??? ???????????? ??? ????????? ??????
        }
        binding.normalVoteList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.normalVoteList.visibility = View.VISIBLE
        binding.photoVoteList.visibility = View.GONE
        binding.normalVoteList.adapter = normalVoteAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun normalSetObserver(
        hashtagTxtList: MutableList<TextView>,
        hashtagCardList: MutableList<CardView>
    ) {

        viewModel.retrofitViewPolls.observe(this, {

            viewModel.retrofitViewPolls.value?.let { it1 -> normalVoteAdapter.setData(it1)

                // ??????
                binding.title.text = it1.viewPollsItem.contents

                // ??????
                when (it1.viewPollsItem.tier) {
                    1 -> binding.tier.setImageResource(R.drawable.layout_tier1)
                    2 -> binding.tier.setImageResource(R.drawable.layout_tier2)
                    3 -> binding.tier.setImageResource(R.drawable.layout_tier3)
                    4 -> binding.tier.setImageResource(R.drawable.layout_tier4)
                    5 -> binding.tier.setImageResource(R.drawable.layout_tier5)
                }

                // ?????????
                for (i in 0 until it1.viewPollsItem.hashTag.size){
                    hashtagCardList[i].visibility = View.VISIBLE
                    hashtagTxtList[i].text = "#${it1.viewPollsItem.hashTag[i].name}"

                }

                // ??????
                if(it1.viewPollsItem.showNick) binding.nick.text = it1.viewPollsItem.nick else binding.nick.text = "anonymous"


                // ????????????
                if (it1.viewPollsItem.myBallots == null || voteState) {
                    binding.voteBtn.visibility = View.VISIBLE
                    binding.reVoteBtn.visibility = View.GONE
                    binding.statisticsBtn.visibility = View.GONE
                    binding.commentList.visibility = View.GONE
                    binding.commentCnt.visibility = View.GONE
                    binding.commentIcon.visibility = View.GONE
                    binding.keyboard.visibility = View.GONE


                } else{

                    binding.voteBtn.visibility = View.GONE
                    binding.reVoteBtn.visibility = View.VISIBLE
                    binding.statisticsBtn.visibility = View.VISIBLE
                    binding.keyboard.visibility = View.VISIBLE


                    // ????????? ??????
                    if(it1.viewPollsItem.canRevote) binding.reVoteBtn.visibility = View.VISIBLE else binding.reVoteBtn.visibility = View.GONE

                    // ?????? ??????
                    if (it1.viewPollsItem.canComment){
                        binding.commentList.visibility = View.VISIBLE
                        binding.commentCnt.visibility = View.VISIBLE
                        binding.commentIcon.visibility = View.VISIBLE
                        binding.keyboard.visibility = View.VISIBLE

                    }else{
                        binding.commentList.visibility = View.GONE
                        binding.commentCnt.visibility = View.GONE
                        binding.commentIcon.visibility = View.GONE
                        binding.keyboard.visibility = View.GONE

                    }


                }

                // D-DAY
                val time = it1.viewPollsItem.endTime// ????????? ?????????
                val now = LocalDateTime.now() // ?????? ??????
                //????????? LocalDateTime ?????? ??????
                val convertTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val compareTime = ChronoUnit.DAYS.between(now, convertTime) //????????? ??????

                when {
                    compareTime.equals(0) -> {
                        binding.dDay.text = "D-day"
                    }
                    compareTime > 0 -> {
                        binding.dDay.text = "D-${compareTime}"

                    }
                    else -> {
                        binding.dDay.text = "D+${-compareTime}"
                        binding.reVoteBtn.visibility = View.GONE
                        binding.voteBtn.visibility = View.GONE
                    }
                }

            }
        })
    }

    private fun photoSetView(){
        photoVoteAdapter =  PhotoVoteAdapter(this, voteState).apply {
            setHasStableIds(true) // ??????????????? ??? ???????????? ??? ????????? ??????
        }
        binding.normalVoteList.visibility = View.GONE
        binding.photoVoteList.visibility = View.VISIBLE
        binding.photoVoteList.adapter = photoVoteAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun photoSetObserver(
        hashtagTxtList: MutableList<TextView>,
        hashtagCardList: MutableList<CardView>
    ) {
        viewModel.retrofitViewPolls.observe(this, {

            viewModel.retrofitViewPolls.value?.let { it1 -> photoVoteAdapter.setData(it1)

                for (i in 0 until it1.viewPollsItem.hashTag.size){
                    hashtagCardList[i].visibility = View.VISIBLE
                    hashtagTxtList[i].text = "#${it1.viewPollsItem.hashTag[i].name}"

                }

                binding.title.text = it1.viewPollsItem.contents
                canReVote = it1.viewPollsItem.canRevote
                canComment = it1.viewPollsItem.canComment


                // ??????
                if(it1.viewPollsItem.showNick) binding.nick.text = it1.viewPollsItem.nick else binding.nick.text = "anonymous"


                // ????????????
                if (it1.viewPollsItem.myBallots == null || voteState) {
                    binding.voteBtn.visibility = View.VISIBLE
                    binding.reVoteBtn.visibility = View.GONE
                    binding.statisticsBtn.visibility = View.GONE
                    binding.commentList.visibility = View.GONE
                    binding.commentCnt.visibility = View.GONE
                    binding.commentIcon.visibility = View.GONE
                    binding.keyboard.visibility = View.GONE

                } else{
                    binding.voteBtn.visibility = View.GONE
                    binding.reVoteBtn.visibility = View.VISIBLE
                    binding.keyboard.visibility = View.VISIBLE
                    binding.statisticsBtn.visibility = View.VISIBLE



                    // ????????? ??????
                    if(it1.viewPollsItem.canRevote) binding.reVoteBtn.visibility = View.VISIBLE else binding.reVoteBtn.visibility = View.GONE

                    // ?????? ??????
                    if (it1.viewPollsItem.canComment){
                        binding.commentList.visibility = View.VISIBLE
                        binding.commentCnt.visibility = View.VISIBLE
                        binding.commentIcon.visibility = View.VISIBLE
                        binding.keyboard.visibility = View.VISIBLE

                    }else{
                        binding.commentList.visibility = View.GONE
                        binding.commentCnt.visibility = View.GONE
                        binding.commentIcon.visibility = View.GONE
                        binding.keyboard.visibility = View.GONE

                    }


                }



                // D-DAY
                val time = it1.viewPollsItem.endTime// ????????? ?????????
                val now = LocalDateTime.now() // ?????? ??????
                //????????? LocalDateTime ?????? ??????
                val convertTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val compareTime = ChronoUnit.DAYS.between(now, convertTime) //?????? ??????



                when {
                    compareTime < 0 -> {
                        binding.dDay.text = "D+${-compareTime}"
                        binding.reVoteBtn.visibility = View.GONE
                        binding.voteBtn.visibility = View.GONE


                    }
                    compareTime > 0 -> {
                        binding.dDay.text = "D-${compareTime}"

                    }
                    else -> {
                        binding.dDay.text = "D-day"



                    }
                }
                


            }
        })
    }

    private fun commentSetView(){

        commentsAdapter =  CommentsAdapter().apply {
            setHasStableIds(true) // ??????????????? ??? ???????????? ??? ????????? ??????
        }
        binding.commentList.adapter = commentsAdapter



    }

    private fun commentSetObserver() {
        viewModel.retrofitCommentList.observe(this, {

            viewModel.retrofitCommentList.value?.let { it1 -> commentsAdapter.setData(it1)
                binding.commentCnt.text = it1.commentItem.size.toString()
            }
        })
    }

    private fun myPollsSetObserver(id: Int) {
        viewModel.retrofitMyPolls.observe(this, {
            viewModel.retrofitMyPolls.value?.let { it1 ->
                if (it1.pollsItem.find { it.id == id } != null) binding.deleteBtn.visibility = View.VISIBLE else binding.deleteBtn.visibility = View.GONE
            }
        })
    }

    private fun onClickAllDeleteDialogButton(id : Int){

        // ??????????????? ??????
        val mHandler = Handler(Looper.getMainLooper())
        mHandler.post {
            val ad = AlertDialog.Builder(this@VoteActivity)
            ad.setIcon(R.drawable.logo)
            ad.setTitle("Delete Vote")
            ad.setMessage("Are you sure you want to delete your vote?\nVoting content and information will be lost when a vote is deleted.")

            // ????????????
            ad.setPositiveButton("Done") { _, _ -> allDelete(id) }
            // ????????????
            ad.setNegativeButton("cancel") { dialog, _ -> dialog.dismiss() }

            ad.show()
        }
    }


    private fun allDelete(id : Int){

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deletePollsRetrofit(id)
        }
        val intent = Intent(this@VoteActivity, PollsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)


        Toast.makeText(this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()


    }



    /***
     * ????????? ??? ?????? background, text ?????? ???????????????.
     * Activity?????? ??? ????????? ????????? ?????? background??? ?????? ??????
     * Adapter?????? ??? ????????? ????????? ?????? background, text ??? ?????? ????????? ??? ??? ????????? ?????? item??? ?????? ???????????? ??? ?????? ?????? ????????? ?????? ??? ??????.
     * ??????????????? ?????????????????? ????????? ?????????..
     */
    override fun onNormalVoteClickListener(
        item: ItemComponent,
        position: Int,
        isSingleVote: Boolean,
        myBallots: ArrayList<Int>?
    ) {
        // ????????? ?????? ????????????, ???????????? ???????????????
        if (myBallots == null || voteState){
            // ?????? ??????
            if (isSingleVote){
                // ???????????? ????????????
                if (checkVote.find { it == item.itemNum } == null) {
                    binding.normalVoteList.children.iterator().forEach { item ->

                        item.setBackgroundResource(R.drawable.layout_unselect_normal_poll_background)
                    }
                    checkVote.clear()
                    checkVote.add(item.itemNum)
                    binding.normalVoteList[position].setBackgroundResource(R.drawable.layout_select_normal_poll_background)

                } else {

                    binding.normalVoteList[position].setBackgroundResource(R.drawable.layout_unselect_normal_poll_background)
                    checkVote.remove(item.itemNum)
                }
                // ?????? ??????
            }else{
                // ????????? ?????? ????????????
                if (checkVote.find { it == item.itemNum } == null) {
                    binding.normalVoteList[position].setBackgroundResource(R.drawable.layout_select_normal_poll_background)
                    checkVote.add(item.itemNum)

                    // ????????? ????????????
                } else {

                    binding.normalVoteList[position].setBackgroundResource(R.drawable.layout_unselect_normal_poll_background)
                    checkVote.remove(item.itemNum)

                }
            }
        }

    }

    override fun onPhotoVoteClickListener(
        item: ItemComponent,
        position: Int,
        isSingleVote: Boolean,
        myBallots: ArrayList<Int>?
    ) {

        if (myBallots == null || voteState){
            // ?????? ??????
            if (isSingleVote){
                // ???????????? ????????????
                if (checkVote.find { it == item.itemNum } == null) {
                    binding.photoVoteList.children.iterator().forEach { item ->

                        item.setBackgroundResource(R.drawable.layout_unselect_normal_poll_background)
                    }

                    checkVote.clear()
                    checkVote.add(item.itemNum)
                    binding.photoVoteList[position].setBackgroundResource(R.drawable.layout_select_normal_poll_background)


                } else {

                    binding.photoVoteList[position].setBackgroundResource(R.drawable.layout_unselect_normal_poll_background)
                    checkVote.remove(item.itemNum)
                }

                // ?????? ??????
            }else{

                // ????????? ?????? ????????????
                if (checkVote.find { it == item.itemNum } == null) {
                    checkVote.add(item.itemNum)
                    binding.photoVoteList[position].setBackgroundResource(R.drawable.layout_select_normal_poll_background)

                    // ????????? ????????????
                } else {

                    binding.photoVoteList[position].setBackgroundResource(R.drawable.layout_unselect_normal_poll_background)
                    checkVote.remove(item.itemNum)
                }


            }
        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{ // ?????? ??????
                binding.mainDrawerLayout.openDrawer(GravityCompat.START)    // ??????????????? ????????? ??????
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.mainDrawerLayout.closeDrawers()
        when(item.itemId){
            R.id.mainPageDrawer-> {
                startActivity( Intent(this@VoteActivity, HomeActivity::class.java))

            }
            R.id.allPollsDrawer-> {
                startActivity( Intent(this@VoteActivity, PollsActivity::class.java))

            }
            R.id.hotPollsDrawer-> {
                startActivity( Intent(this@VoteActivity, HotPollsActivity::class.java))

            }
            R.id.searchDrawer-> {
                startActivity( Intent(this@VoteActivity, SearchActivity::class.java))

            }
            R.id.myPageDrawer-> {
                // My Page ??????
                startActivity( Intent(this@VoteActivity, MyPageActivity::class.java))

            }
            R.id.newPollDrawer-> {
                // My Page ??????
                startActivity(Intent(this@VoteActivity, NewPollActivity::class.java))

            }

            R.id.logoutDrawer-> {
                // ????????????
//            startActivity(Intent(this@HomeActivity, MyPageActivity::class.java))

            }

        }
        return false
    }

    override fun onBackPressed() { //???????????? ??????
        if(binding.mainDrawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.mainDrawerLayout.closeDrawers()

        } else{
            super.onBackPressed()
        }
    }


}