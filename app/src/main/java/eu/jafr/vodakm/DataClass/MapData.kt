package eu.jafr.vodakm.DataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MapData(
    val address: String,
    val lat: Double,
    val lon: Double,

    var btnType: String

) : Parcelable
