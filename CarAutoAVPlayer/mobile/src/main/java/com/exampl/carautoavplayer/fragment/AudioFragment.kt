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
import com.exampl.carautoavplayer.adapter.MediaListAdapter
import com.exampl.carautoavplayer.databinding.FragmentAudioBinding
import com.exampl.carautoavplayer.model.MediaListModel
import com.exampl.carautoavplayer.model.msg
import com.google.gson.Gson
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AudioFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AudioFragment : Fragment() {

    private var _binding: FragmentAudioBinding? = null


    private val binding get() = _binding!!

    private var mediaListModel: MediaListModel? = null
    private var mediaListModelArrayList: ArrayList<MediaListModel>? = null
    private lateinit var mediaListAdapter: MediaListAdapter
    val TAG = "MAIN ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        mediaListModelArrayList = ArrayList<MediaListModel>()
        initialize()
        return binding.root

    }

    private fun initialize() {
        readMediaFiles()
        mediaListAdapter = MediaListAdapter(mediaListModelArrayList!!, requireContext())
        if (mediaListModelArrayList!!.isEmpty()) {
            Log.d(TAG, mediaListModelArrayList!!.size.toString())
        } else {

            Log.d("VALUE", mediaListModelArrayList!!.size.toString())
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
            binding.rvList.layoutManager = layoutManager


            // mediaListModelArrayList1.addAll(mediaListModelArrayList.;
            binding.rvList.adapter = mediaListAdapter
            Log.d(TAG, mediaListModelArrayList!!.size.toString())
        }

        mediaListAdapter.setOnClickListener(object : MediaListAdapter.OnClickListener {
            override fun onClick(position: Int, model: MediaListModel) {
                val sharedPreferences = requireContext().getSharedPreferences(
                    "shared preferences",
                    Context.MODE_PRIVATE
                )
                val prefsEditor: SharedPreferences.Editor = sharedPreferences.edit()
                val gson = Gson()
                val json = gson.toJson(mediaListModelArrayList)
                prefsEditor.putString("MyObject", json)
                prefsEditor.commit()

                val intent = Intent(context, PlayActivity::class.java)
                intent.putExtra("Name", model.name)
                intent.putExtra("URI", model.url)
                intent.putExtra("IMAGE", model.albumid)
                intent.putExtra("DURATION", model.duration)
                intent.putExtra("POS", position)
                startActivity(intent)
            }


        })
    }


    private fun readMediaFiles() {
        val contentResolver = requireContext().contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val music = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val sort = MediaStore.Audio.Media.ALBUM + " ASC"

        val projection = arrayOf(
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION

        )

        val cursor = contentResolver.query(uri, projection, music, null, sort)
        try {
            if (cursor != null) {
                msg.log(cursor.count.toString())
                Log.d("MEDIA MODEL", cursor.count.toString())
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        @SuppressLint("Range") val path =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                        @SuppressLint("Range") val title =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                        @SuppressLint("Range") val albumid =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID))
                        @SuppressLint("Range") val duration =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                        // String thumbNail=cursor.get
                        mediaListModel = MediaListModel(albumid.toLong(), path, title, duration)
                        Log.d("MEDIA MODEL", mediaListModel!!.name.toString())
                        if (File(mediaListModel!!.url.toString()).exists())
                            mediaListModelArrayList?.add(mediaListModel!!)

                    }
                }

            }
        } catch (e: Exception) {
            cursor?.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}