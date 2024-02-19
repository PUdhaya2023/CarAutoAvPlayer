package com.exampl.carautoavplayer.activity

import android.R.attr.tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.exampl.carautoavplayer.R
import com.exampl.carautoavplayer.databinding.ActivityVideoPlayBinding
import com.exampl.carautoavplayer.model.VideoListModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


class VideoPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayBinding
    private lateinit var mLibVLC: LibVLC
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var vlcVideoLayout: VLCVideoLayout

    var uri: String? = null
    var name: String? = null
    var duration: String? = null
    var position: Int? = null

    var currentIndex: Int = -1
    var currentSong: VideoListModel? = null
    private lateinit var videoList: ArrayList<VideoListModel>
    val mHandler = Handler(Looper.getMainLooper())
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mLibVLC = LibVLC(this)
        mMediaPlayer = MediaPlayer(mLibVLC)
        getData()

        currentIndex = position!!

        setResource()


    }

    private fun setResource() {
        currentSong = videoList.get(currentIndex)
        binding.totalTime.text = convertToMMSS(videoList.get(currentIndex).duration.toString())

        binding.pausePlay.setOnClickListener(View.OnClickListener
        { pausePlay() })
        binding.next.setOnClickListener(View.OnClickListener
        { playnextSong() })
        binding.previous.setOnClickListener(View.OnClickListener { playPreviousSong() })
        binding.rewind.setOnClickListener(View.OnClickListener {
            rewind()
        })
        binding.backward.setOnClickListener(View.OnClickListener { backWard() })

        playVideo()
        if (mMediaPlayer.isPlaying) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    private fun calculate(progress: Int): Long {
        val newPos = progress.toFloat() / 100 * mMediaPlayer.media?.duration!!
        Log.d("PAUSE:NEw",newPos.toString())
        return newPos.toLong()
    }

    private fun getData() {
        val intent = intent
        uri = intent.getStringExtra("URI")
        Log.d("URI", uri.toString())
        name = intent.getStringExtra("Name")
        duration = intent.getStringExtra("DURATION")
        position = intent.getIntExtra("POS", 0)

        val mPrefs = getSharedPreferences("shared preferences", MODE_PRIVATE)


        val gson = Gson()
        val json = mPrefs.getString("MyList", null)
        val type: Type = object : TypeToken<ArrayList<VideoListModel?>?>() {}.type

        // in below line we are getting data from gson
        // and saving it to our array list

        videoList = gson.fromJson<Any>(json, type) as ArrayList<VideoListModel>

        Log.d("SONG_LIST", videoList.size.toString())

    }

    fun convertToMMSS(duration: String): String {
        var millis: Long = duration.toLong()
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )

    }

    private fun playVideo() {

        mLibVLC = LibVLC(this)
        mMediaPlayer = MediaPlayer(mLibVLC)
        vlcVideoLayout = VLCVideoLayout(this)
        mMediaPlayer.attachViews(binding.videoPlay, null, false, false)

        val media = Media(mLibVLC, videoList.get(currentIndex).url)
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")

        mMediaPlayer.media = media
//        mMediaPlayer.title=videoList.get(currentIndex).tile.toInt()
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
        // mMediaPlayer.getVLCVout().addCallback(this)
        media.release()
        mMediaPlayer.play()


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
                // Log.d()
                val progress = (currentPosition.toFloat() / totalDuration * 100).toInt()
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
                        Log.d("PRO:",it.progress.toString())
                        mMediaPlayer.time = calculate(it.progress)
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mHandler.removeCallbacksAndMessages(null)
                handler.removeCallbacksAndMessages(null)
                finish()

            }

        })
    }

    private fun playnextSong() {
        if (currentIndex == videoList.size.minus(1))
            return
        currentIndex += 1
        mMediaPlayer.release()
        setResource()
    }

    private fun playPreviousSong() {
        if (currentIndex == 0)
            return
        else
            currentIndex -= 1
        mMediaPlayer.release()
        setResource()


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

    var pauseTime: Long? = null
    private fun pausePlay() {
        if (mMediaPlayer.isPlaying) {
            binding.pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_filled_24)
            pauseTime = mMediaPlayer.playerState.toLong()
            Log.d("PAUSE IF", pauseTime.toString())
            mMediaPlayer.pause()


        } else {
            binding.pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            // mMediaPlayer.release()
            Log.d("PAUSE", pauseTime.toString())
          //  mMediaPlayer.setTime(pauseTime!!)
            //mMediaPlayer.setPosition(pauseTime)
           mMediaPlayer.play()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        mLibVLC.release()
    }

    override fun onStop() {
        super.onStop()
        mMediaPlayer.stop()
        mMediaPlayer.detachViews()
    }


}