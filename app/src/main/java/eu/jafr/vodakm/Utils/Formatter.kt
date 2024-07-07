package eu.jafr.vodakm.Utils

import java.util.Locale

class Formatter {
    fun toKm(km: Double): String {
        return String.format(Locale.ROOT, "%4.2f km", km)
    }
}