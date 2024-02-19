package com.exampl.carautoavplayer.model

import android.os.Parcelable

import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaListModel(
    var albumid: Long,
    var url: String?,
    var name: String?,
    var duration: String?
) : Parcelable {


}