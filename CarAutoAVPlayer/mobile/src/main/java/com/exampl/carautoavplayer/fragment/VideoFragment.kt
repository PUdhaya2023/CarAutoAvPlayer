package com.exampl.carautoavplayer.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exampl.carautoavplayer.activity.PlayActivity
import com.exampl.carautoavplayer.activity.VideoPlayActivity
import com.exampl.carautoavplayer.adapter.MediaListAdapter
import com.exampl.carautoavplayer.adapter.VideoListAdapter
import com.exampl.carautoavplayer.databinding.FragmentVideoBinding
import com.exampl.carautoavplayer.model.MediaListModel
import com.exampl.carautoavplayer.model.VideoListModel
import com.exampl.carautoavplayer.model.msg
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentVideoBinding? = null


    private val binding get() = _binding!!

    private lateinit var mLibVLC: LibVLC
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var vlcVideoLayout: VLCVideoLayout
    private lateinit var args: ArrayList<String>

    private lateinit var videoListModel: VideoListModel
    private lateinit var videoListModelArrayList: ArrayList<VideoListModel>
    private lateinit var videoListAdapter: VideoListAdapter
    private val USE_TEXTURE_VIEW: Boolean = false
    private val ENABLE_SUBTITLES = false

    /*private val resultLauncher = registerForActivityResult(GetContent()) { uri: Uri? ->
        playVideo(uri)
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        videoListModelArrayList = ArrayList<VideoListModel>()
        // mLibVLC = LibVLC(context)
        // mMediaPlayer = MediaPlayer(mLibVLC)
        //binding.openButton.setOnClickListener(View.OnClickListener {
        //  resultLauncher.launch("video/*")
        //})
        initialize()

        return binding.root

    }

    private fun initialize() {
       readMediaFiles()
        videoListAdapter = VideoListAdapter(videoListModelArrayList, requireContext())
        if (videoListModelArrayList.isEmpty()) {
            Log.d("VIDEO", videoListModelArrayList.size.toString())
        } else {

            Log.d("VALUE", videoListModelArrayList.size.toString())
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
            binding.rvVideoList.layoutManager = layoutManager
            binding.rvVideoList.adapter = videoListAdapter
            Log.d("VIDEO", videoListModelArrayList.size.toString())
        }
        videoListAdapter.setOnClickListener(object :VideoListAdapter.OnClickListener{
            override fun onClick(position: Int, model: VideoListModel) {


                val sharedPreferences = requireContext().getSharedPreferences(
                    "shared preferences",
                    Context.MODE_PRIVATE
                )
                val prefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
                val gson = Gson()
                val json = gson.toJson(videoListModelArrayList )
                prefsEditor.putString("MyList", json)
                prefsEditor.commit()

                val intent = Intent(context, VideoPlayActivity::class.java)
                intent.putExtra("Name", model.tile)
                intent.putExtra("URI", model.url)
                intent.putExtra("DURATION", model.duration)
                intent.putExtra("POS", position)
                startActivity(intent)
            }

        })

    }

    @SuppressLint("Recycle")
    private fun readMediaFiles() {
        val contentResolver = requireContext().contentResolver
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val sort = MediaStore.Video.Media.ALBUM + " ASC"

        val projection = arrayOf(
            MediaStore.Video.VideoColumns.TITLE,
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DURATION,
            MediaStore.Video.VideoColumns.DATA


        )

        //val selection = MediaStore.Video.VideoColumns.DATA + "=?"
        val cursor = contentResolver.query(uri, projection, null, null, sort)
        try {
            if (cursor != null) {
                msg.log(cursor.count.toString())

                if (cursor.count > 0) {
                    Log.d("CURSOR", cursor.count.toString())
                    while (cursor.moveToNext()) {
                        @SuppressLint("Range") val title =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE))
                        @SuppressLint("Range") val path =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA))
                        @SuppressLint("Range") val duration =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION))
                        videoListModel = VideoListModel(title, path, duration)
                        videoListModelArrayList.add(videoListModel)
                    }
                }
            }
        } catch (e: Exception) {
            cursor?.close()
        }
    }
    /*

    mLibVLC = new LibVLC(getContext(), args);
    mMediaPlayer = new MediaPlayer(mLibVLC);
    final IVLCVout vout = mMediaPlayer.getVLCVout();
    vout.setWindowSize(mVideoLayout.getWidth(),
    mVideoLayout.getHeight());
    mMediaPlayer.attachViews(mVideoLayout,
    null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW); try
    { Uri uri = Uri.parse(Content.getPlayHost(device_sn));
        final Media media = new Media(mLibVLC, uri);
        media.setHWDecoderEnabled(true,true);
        mMediaPlayer.setMedia(media); media.release(); }
    catch (Exception e) { throw new RuntimeException("Invalid URL"); }
    mVideoLayout.setVisibility(View.VISIBLE);
    mLlLoading.setVisibility(View.INVISIBLE);
    mMediaPlayer.play();*/

    /*private fun playVideo(uri: Uri?) {
        val contentResolver = requireContext().contentResolver
        if (uri === null) {
            return
        }
        val fd = contentResolver.openFileDescriptor(uri, "r")

        mMediaPlayer.attachViews(binding.videoLayout, null, false, false)

        val media = Media(mLibVLC, fd!!.fileDescriptor)
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")

        mMediaPlayer.media = media
        media.release()
        mMediaPlayer.play()

        *//*var vout: IVLCVout = mMediaPlayer.getVLCVout();
        vout.setWindowSize(
            vlcVideoLayout.getWidth(),
            vlcVideoLayout.getHeight()
        );
        mMediaPlayer.attachViews(
            vlcVideoLayout,
            null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW
        );
        try {
            var uri: Uri = Uri.parse(Content.getPlayHost(device_sn));
            val media: Media = Media(mLibVLC, uri);
            media.setHWDecoderEnabled(true, true);
            mMediaPlayer.setMedia(media); media.release();
        } catch (e:Exception) {
            throw
            RuntimeException("Invalid URL"); }
        binding.videoLayout.setVisibility(View.VISIBLE);
        // mLlLoading.setVisibility(View.INVISIBLE);
        mMediaPlayer.play();*//*
    }*/
    override fun onStop() {
        super.onStop()


    }

    override fun onDestroy() {
        super.onDestroy()


    }

}