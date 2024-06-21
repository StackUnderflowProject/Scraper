package interfaces

import org.bson.types.ObjectId
import util.LocationUtil

interface IStadium {
    val name: String
    val teamId: ObjectId
    val capacity: UShort?
    val location: LocationUtil.Location?
    val buildYear: UShort?
    val imagePath: String?
    val id: ObjectId
    val season: UShort

    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}