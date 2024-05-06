package interfaces

import org.bson.types.ObjectId
import java.util.*

interface IStadium {
    val name: String
    val teamId: ObjectId
    val capacity: UShort?
    val location: String?
    val buildYear: UShort?
    val imagePath: String?
    val id: ObjectId

    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}