package interfaces

import org.bson.types.ObjectId

interface IStanding {
    val place: UShort
    val team: ObjectId
    val gamesPlayed: UShort
    val wins: UShort
    val losses: UShort
    val goalsScored: UShort
    val goalsConceded: UShort
    val points: UShort
    val id: ObjectId
    val season: UShort

    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}