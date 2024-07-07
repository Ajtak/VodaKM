package eu.jafr.vodakm.GeoParser

import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

interface IRiver  : BaseRealmObject{

    var coords: GPSCoords?
}