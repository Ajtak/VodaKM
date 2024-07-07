package eu.jafr.vodakm.GeoParser

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class GPSCoords : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    @PersistedName("order_id")
    var OrderId : Int = 0

    @PersistedName("latitude")
    var Latitude: Double = 0.0;

    @PersistedName("longitude")
    var Longitude: Double = 0.0;

    @PersistedName("second_latitude")
    var SecondLat: Double? = null;

    @PersistedName("second_longitude")
    var SeconLong: Double? = null;

    @PersistedName("distance")
    var Distance: Double? = null;

}
