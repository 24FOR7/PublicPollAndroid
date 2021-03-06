package com.junjange.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.junjange.myapplication.R
import com.junjange.myapplication.data.*
import com.junjange.myapplication.databinding.ItemRecyclerHotPollsBinding
import com.junjange.myapplication.network.PollsObject
import com.junjange.myapplication.ui.view.VoteActivity
import com.junjange.myapplication.utils.API

class HotPollsAdapter(val context: Context) : RecyclerView.Adapter<HotPollsAdapter.ViewHolder>()   {

    private var items: HotPolls = HotPolls(ArrayList())


    // 뷰 홀더 만들어서 반환
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecyclerHotPollsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // 전달받은 위치의 아이템 연결
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rankNumber(position)

        holder.clickItem(items.hotPollsItem[0].polls[position])
        holder.setItem(items.hotPollsItem[0].polls[position])
    }

    // 뷰 홀더 설정
    inner class ViewHolder(private val binding: ItemRecyclerHotPollsBinding) : RecyclerView.ViewHolder(binding.root) {


        fun clickItem(item: HotPollsComponent){
            binding.hotPollCardView.setOnClickListener {

                // 원하는 화면 연결
                Intent(context, VoteActivity::class.java).apply {
                    // 데이터 전달
                    putExtra("id", item.id)
                    putExtra("presentImagePath", item.presentImagePath)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {
                    //액티비티 열기
                    context.startActivity(this)
                }
            }

        }

        fun setItem(item: HotPollsComponent){
            binding.title.text =  item.contents

            when {
                item.hashTags.size >= 2 -> {
                    binding.hashtagTxt1.text = item.hashTags[0].name
                    binding.hashtagCard1.visibility = View.VISIBLE
                    binding.hashtagTxt2.text = item.hashTags[1].name
                    binding.hashtagCard2.visibility = View.VISIBLE
                }
                item.hashTags.size == 1 -> {
                    binding.hashtagTxt1.text = item.hashTags[0].name
                    binding.hashtagCard1.visibility = View.VISIBLE
                    binding.hashtagCard2.visibility = View.GONE

                }
                else -> {
                    binding.hashtagCard1.visibility = View.GONE
                    binding.hashtagCard2.visibility = View.GONE


                }
            }

            // 이미지 여부에 따라 사진 투표 호출
            if(item.presentImagePath != null){

                val token = PollsObject.token
                val url = "${API.BASE_URL}${item.presentImagePath}"
                val glideUrl = GlideUrl(url) { mapOf(Pair("Authorization", token))}
                Glide.with(binding.pollImage.context).load(glideUrl).error(R.drawable.image_default).into(binding.pollImage)
                binding.pollImage.visibility = View.VISIBLE
                binding.title.setTextSize(Dimension.SP, 20F)


            }else{
                binding.pollImage.visibility = View.GONE
                binding.title.setTextSize(Dimension.SP, 16F)


            }
        }


        // 랭킹에 따라 이미지 변경
        @SuppressLint("SetTextI18n")
        fun rankNumber(position: Int){
            binding.rank.text = "${position + 1}"
            when (position) {
                0 -> binding.rank.setBackgroundResource(R.drawable.layout_rank1)
                1 ->  binding.rank.setBackgroundResource(R.drawable.layout_rank2)
                2 -> binding.rank.setBackgroundResource(R.drawable.layout_rank3)
                else -> binding.rank.setBackgroundResource(R.drawable.layout_rank4)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    @SuppressLint("NotifyDataSetChanged")
    internal fun setData(newItems: HotPolls) {

        this.items = newItems
        notifyDataSetChanged()

    }
    // 아이템 개수
    override fun getItemCount(): Int {
        return if (items.hotPollsItem.isEmpty()) 0 else items.hotPollsItem[0].polls.size
    }


}