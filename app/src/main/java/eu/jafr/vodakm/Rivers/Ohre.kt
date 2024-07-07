package eu.jafr.vodakm.Rivers

import eu.jafr.vodakm.GeoParser.GPSCoords
import eu.jafr.vodakm.GeoParser.IRiver
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Ohre : RealmObject, IRiver {

    override var coords: GPSCoords? = null

}