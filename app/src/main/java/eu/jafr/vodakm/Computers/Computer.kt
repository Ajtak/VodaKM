package eu.jafr.vodakm.Computers

import eu.jafr.vodakm.Rivers.Ohre
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt

class Computer {

    private fun computeDistanceBetweeen2Points(
        point1: Pair<Double, Double>,
        point2: Pair<Double, Double>
    ): Double {
        val R = 6371000.0  // Průměrný poloměr Země v metrech

        // Převod zeměpisných šířek a délek z stupňů na radiány
        val lat1Rad = Math.toRadians(point1.first)
        val lon1Rad = Math.toRadians(point1.second)
        val lat2Rad = Math.toRadians(point2.first)
        val lon2Rad = Math.toRadians(point2.second)

        // Equirectangular approximation
        val x = (lon2Rad - lon1Rad) * cos((lat1Rad + lat2Rad) / 2.0)
        val y = lat2Rad - lat1Rad
        val d = sqrt(x.pow(2) + y.pow(2)) * R

        return d
    }

    fun computeTotalLength(pointsInRange: List<Pair<Double, Double>>): Double {
        var totalDistance = 0.0


        for (i in 0 until pointsInRange.size - 1) {

            val coord1 = pointsInRange[i]
            val coord2 = pointsInRange[i + 1]

            val distance = this.computeDistanceBetweeen2Points(coord1, coord2)

            totalDistance += distance / 1000
        }

        return totalDistance
    }

    fun getPointsBetween(
        coords: List<Pair<Double, Double>>,
        start: Pair<Double, Double>,
        end: Pair<Double, Double>
    ): List<Pair<Double, Double>> {

        val startIndex = coords.indexOf(start)
        val endIndex = coords.indexOf(end)

        return coords.subList(startIndex, endIndex)
    }

    suspend fun getNearestPoint(
        points: RealmResults<Ohre>,
        selectedPoint: Pair<Double, Double>
    ): Int {
        return coroutineScope {
            val deferredList: List<Deferred<Pair<Int, Double>>> =
                points.map { ohre ->
                    async {
                        // Extrahování potřebných hodnot z Ohre objektu
                        val point = Pair(ohre.coords!!.Latitude, ohre.coords!!.Longitude)
                        val distance = computeDistanceBetweeen2Points(selectedPoint, point)
                        Pair(ohre.coords!!.OrderId, distance)
                    }
                }

            val nearestPair = deferredList.awaitAll().minByOrNull { it.second }
            nearestPair?.first
                ?: throw IllegalStateException("RealmResults<Ohre> neobsahuje žádné položky")
        }
    }

    fun countDistance(points: RealmResults<Ohre>): Double {
        var distance = 0.0
        points.forEach {
            val dist = it.coords?.Distance ?: 0.0
            distance += dist;
        }

        // Musím odečíst poslední vzdálenost,
        // protože ta už k dalšímu bodu se nesmí počítat,
        // to už se nejede
        distance -= points.last().coords?.Distance ?: 0.0

        return distance
    }
}