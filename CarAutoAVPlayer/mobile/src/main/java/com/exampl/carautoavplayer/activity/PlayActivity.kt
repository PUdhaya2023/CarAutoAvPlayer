package com.exampl.carautoavplayer.activity

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.exampl.carautoavplayer.R
import com.exampl.carautoavplayer.databinding.ActivityPlayBinding
import com.exampl.carautoavplayer.model.MediaListModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


class PlayActivity : AppCompatActivity() {
    private var mLibVLC: LibVLC? = null
    private lateinit var mMediaPlayer: MediaPlayer

    var uri: String? = null
    var name: String? = null
    var image: Long? = null
    var duration: String? = null
    var position: Int? = null

    private var vlcVideoLayout: VLCVideoLayout? = null
    private lateinit var songList: ArrayList<MediaListModel>
    private var mediaListModel: MediaListModel? = null
    var currentSong: MediaListModel? = null
    lateinit var handler: Handler
    lateinit var mHandler: Handler
    var currentIndex: Int = -1


    private lateinit var binding: ActivityPlayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler(Looper.getMainLooper())
        mHandler = Handler(Looper.getMainLooper())

        if (Build.VERSION.SDK_INT >= 33) {
            getData()
        }

        currentIndex = position!!

        setResourceWithMusic()


    }


    @RequiresApi(33)
    private fun getData() {
        val intent = intent
        uri = intent.getStringExtra("URI")
        Log.d("URI", uri.toString())
        name = intent.getStringExtra("Name")
        image = intent.getLongExtra("IMAGE", 0)
        duration = intent.getStringExtra("DURATION")
        position = intent.getIntExtra("POS", 0)

        val mPrefs = getSharedPreferences("shared preferences", MODE_PRIVATE)


        val gson = Gson()
        val json = mPrefs.getString("MyObject", null)
        val type: Type = object : TypeToken<ArrayList<MediaListModel?>?>() {}.type

        // in below line we are getting data from gson
        // and saving it to our array list

        songList = gson.fromJson<Any>(json, type) as ArrayList<MediaListModel>

        Log.d("SONG_LIST", songList.size.toString())


    }

    private fun setResourceWithMusic() {
        currentSong = songList.get(currentIndex)
        val sArtworkUri = Uri
            .parse("content://media/external/audio/albumart")
        val albumArtUri =
            ContentUris.withAppendedId(sArtworkUri, songList.get(currentIndex).albumid)

        Picasso.get().load(albumArtUri).placeholder(R.drawable.ic_baseline_music_note_24)
            .error(R.drawable.ic_baseline_music_note_24).into(binding.mucisIconBig)

        binding.songTitle.isSelected = true

        binding.songTitle.text = songList.get(currentIndex).name
        binding.totalTime.text = convertToMMSS(songList.get(currentIndex).duration.toString())

        binding.pausePlay.setOnClickListener(View.OnClickListener
        { pausePlay() })
        binding.next.setOnClickListener(View.OnClickListener
        { playnextSong() })
        binding.previous.setOnClickListener(View.OnClickListener { playPreviousSong() })
        binding.rewind.setOnClickListener(View.OnClickListener {
            rewind()
        })
        binding.backward.setOnClickListener(View.OnClickListener { backWard() })

        createAudioPlayerLib()

        val currentTime = object : Runnable {
            override fun run() {
                var currentTime = convertToMMSS(mMediaPlayer.time.toString())

                binding.currentTime.text = currentTime
                mHandler.postDelayed(this, 1000)

            }

        }
        mHandler.postDelayed(currentTime, 1000)


        val updateSeekBar = object : Runnable {
            override fun run() {
                val currentPosition = mMediaPlayer.time
                Log.d("TIME", mMediaPlayer.time.toString())
                val totalDuration = mMediaPlayer.media?.duration ?: 0
                val progress = (currentPosition.toFloat() / totalDuration * 100).toInt()
                Log.d("TIME:Progress", progress.toString())
                binding.seekBar.max = 100
                binding.seekBar.progress = progress
                val durationString = convertToMMSS(totalDuration.toString())
                val currentPositionString = convertToMMSS(currentPosition.toString())
                binding.currentTime.text = currentPositionString
                binding.totalTime.text = durationString
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(updateSeekBar, 1000)

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    binding.seekBar.let {
                        mMediaPlayer.time = currentProgress(it.progress)
                        mMediaPlayer.play()
                    }
                    handler.postDelayed(updateSeekBar, 1000)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                mMediaPlayer.pause()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mMediaPlayer.play()
            }

        })


    }


    private fun currentProgress(progress: Int): Long {
        val newPos = progress.toFloat() / 100 * mMediaPlayer.media?.duration!!
        return newPos.toLong()
    }


    private fun playnextSong() {
        if (currentIndex == songList.size.minus(1))
            return
        currentIndex += 1
        mMediaPlayer.release()
        setResourceWithMusic()
    }

    private fun playPreviousSong() {
        if (currentIndex == 0)
            return
        else
            currentIndex -= 1
        mMediaPlayer.release()
        setResourceWithMusic()


    }

    private fun rewind() {
        if (mMediaPlayer.isPlaying) {
            Log.d("PLay", mMediaPlayer.time.toString())
            mMediaPlayer.setTime(mMediaPlayer.time - 10000)
            var currentTime = convertToMMSS(mMediaPlayer.time.toString())
            binding.currentTime.text = currentTime

        }
    }

    private fun backWard() {
        if (mMediaPlayer.isPlaying) {
            Log.d("PLay", mMediaPlayer.time.toString())
            mMediaPlayer.setTime(mMediaPlayer.time + 10000)
            var currentTime = convertToMMSS(mMediaPlayer.time.toString())
            binding.currentTime.text = currentTime

        }

    }

    private fun pausePlay() {
        if (mMediaPlayer.isPlaying) {
            binding.pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            mMediaPlayer.pause()


        } else {
            binding.pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            mMediaPlayer.play()

        }


    }

    private fun createAudioPlayerLib() {
        mLibVLC = LibVLC(this)
        mMediaPlayer = MediaPlayer(mLibVLC)
        val media = Media(mLibVLC, songList.get(currentIndex).url)
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")
        mMediaPlayer.media = media
        mMediaPlayer.setEventListener(MediaPlayer.EventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Opening ->
                    Log.i("VIDEO", "Event Opening")
                MediaPlayer.Event.Buffering ->
                    Log.i("VIDEO", "Event Buffering=" + event.buffering)
                MediaPlayer.Event.Stopped -> {
                    Log.i("VIDEO", "Event Stopped")
                    /*if (currentIndex == videoList.size.minus(1))
                        return@EventListener
                    else {
                        currentIndex += 1
                        mMediaPlayer.release()
                        setResource()
                    }*/
                    playnextSong()
                }
            }
        })
        media.release()
        binding.seekBar.setMax(mMediaPlayer.media!!.duration.toInt())
        /*binding.seekBar.setProgress(10)*/

        mMediaPlayer.play()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mHandler.removeCallbacksAndMessages(null)
                handler.removeCallbacksAndMessages(null)
                finish()

            }

        })
    }


    fun convertToMMSS(duration: String): String {
        var millis: Long = duration.toLong()
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )

    }


    override fun onStart() {
        super.onStart()
        // createAudioPlayerLib()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        mLibVLC!!.release()
    }

    override fun onStop() {
        super.onStop()

        mMediaPlayer.stop()
        mMediaPlayer.detachViews()
    }


}

