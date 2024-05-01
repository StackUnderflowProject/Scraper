package model

import java.time.LocalDate
import java.util.*

interface IMatch {
    val home: String
    val score: String?
    val time: String?
    val away: String
    val played: Boolean
    val date: LocalDate
    val location: String
    val stadium: String
    val id: UUID
    
    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}