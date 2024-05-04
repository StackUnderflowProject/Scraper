package interfaces

import java.time.LocalDate
import java.util.*

interface IMatch {
    val home: UUID
    val score: String?
    val time: String?
    val away: UUID
    val played: Boolean
    val date: LocalDate
    val location: String
    val id: UUID
    
    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}