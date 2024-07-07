package eu.jafr.vodakm.GeoParser

import eu.jafr.vodakm.Rivers.Ohre
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.TypedRealmObject
import java.lang.reflect.TypeVariable
import java.util.Locale
import kotlin.reflect.KClass

class RealmHelper() {

    lateinit var realm: Realm;

    init {
        val config = RealmConfiguration.Builder(
            schema = setOf(GPSCoords::class, Ohre::class)
        )
            .name("database.realm")
            .build()
        realm = Realm.open(config)
    }


    public suspend fun <T : IRiver> getRiver(): RealmResults<Ohre> {

        return realm.query(Ohre::class).find()
    }


    fun GetPointsBetween(fPoint: Int, sPoint: Int): RealmResults<Ohre> {
        val condition = "coords.order_id >= %d  AND coords.order_id <= %d"

        val pointsRange =
            realm.query(Ohre::class, String.format(Locale.ROOT, condition, fPoint, sPoint)).find()


        return pointsRange;
    }

}