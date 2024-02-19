package com.exampl.carautoavplayer.adapter

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.exampl.carautoavplayer.R
import com.exampl.carautoavplayer.databinding.RvModelBinding
import com.exampl.carautoavplayer.databinding.VideoListBinding

import com.exampl.carautoavplayer.model.MediaListModel
import com.exampl.carautoavplayer.model.VideoListModel
import com.squareup.picasso.Picasso

class VideoListAdapter(private val vList: List<VideoListModel>, context: Context) :
    RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {
    private var onClickListener: VideoListAdapter.OnClickListener? = null
    var cnxt: Context = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = VideoListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val videoListModel: VideoListModel = vList[holder.absoluteAdapterPosition]
        holder.binding.tvVideoName.text = videoListModel.tile
        /*val sArtworkUri = Uri
            .parse("content://media/external/video/")
        val albumArtUri = ContentUris.withAppendedId(sArtworkUri, videoListModel.path?.toLong()!!)*/
        /*val thumb: Long = holder.layoutPosition.toLong() * 1000
        val options: RequestOptions = RequestOptions().frame(thumb)*/
        /*val requestOptions: RequestOptions= RequestOptions()
        requestOptions.isMemoryCacheable()
        Glide.with(cnxt).load(videoListModel.url).apply(requestOptions).into(holder.binding.videoImg)*/
        holder.binding.tvVideoName.setOnClickListener(View.OnClickListener {
            // Log.d("POS:", position.toString())
            //onItemClick?.invoke(mediaListModel)
            onClickListener?.onClick(position,videoListModel)
        })

    }

    override fun getItemCount(): Int {
        Log.d("SIZE", vList.size.toString())
        return vList.size
    }

    class ViewHolder(val binding:VideoListBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    interface OnClickListener {
        fun onClick(position: Int, model: VideoListModel)
    }
}