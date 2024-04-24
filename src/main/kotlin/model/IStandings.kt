package model

import java.util.*

interface IStandings {
    val place: UShort
    val team: String
    val gamesPlayed: UShort
    val wins: UShort
    val losses: UShort
    val goalsScored: UShort
    val goalsConceded: UShort
    val points: UShort
    val id: UUID
    
    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}