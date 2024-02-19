package com.exampl.carautoavplayer.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.exampl.carautoavplayer.R
import com.exampl.carautoavplayer.activity.PlayActivity
import com.exampl.carautoavplayer.databinding.RvModelBinding
import com.exampl.carautoavplayer.model.MediaListModel
import com.google.gson.Gson
import com.squareup.picasso.Picasso


class MediaListAdapter(private val mList: List<MediaListModel>, context: Context) :
    RecyclerView.Adapter<MediaListAdapter.ViewHolder>() {
   // var onItemClick: ((MediaListModel) -> Unit)? = null
   private var onClickListener: OnClickListener? = null
    var cnxt: Context = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = RvModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(itemBinding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val mediaListModel: MediaListModel = mList[holder.absoluteAdapterPosition]
        holder.binding.tvMediaName.text = mediaListModel.name
        val sArtworkUri = Uri
            .parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(sArtworkUri, mediaListModel.albumid)
        Picasso.get().load(albumArtUri).placeholder(R.drawable.ic_baseline_music_note_24)
            .error(R.drawable.ic_baseline_music_note_24).into(holder.binding.songImg)
        holder.binding.tvMediaName.setOnClickListener(View.OnClickListener {
            // Log.d("POS:", position.toString())
               //onItemClick?.invoke(mediaListModel)
            onClickListener?.onClick(position,mediaListModel)
        })


    }

    override fun getItemCount(): Int {
        Log.d("SIZE", mList.size.toString())
        return mList.size
    }


    class ViewHolder(val binding: RvModelBinding) : RecyclerView.ViewHolder(binding.root) {


    }
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    interface OnClickListener {
        fun onClick(position: Int, model: MediaListModel)
    }
}






