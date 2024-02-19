package com.exampl.carautoavplayer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoListModel(var tile: String, var url: String?, var duration: String?) : Parcelable {

}

