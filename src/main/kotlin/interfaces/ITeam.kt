package interfaces

import org.bson.types.ObjectId

interface ITeam {
    val id: ObjectId
    val name: String
    val director: String
    var coach: String
    var logoPath: String
    val season: UShort

    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}