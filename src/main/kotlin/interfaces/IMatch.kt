package interfaces

import org.bson.types.ObjectId
import java.time.LocalDate

interface IMatch {
    val home: ObjectId
    val score: String?
    val time: String?
    val away: ObjectId
    val played: Boolean
    val date: LocalDate
    val location: String
    val id: ObjectId
    
    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}