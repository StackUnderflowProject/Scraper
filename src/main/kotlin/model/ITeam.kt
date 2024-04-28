package model

import java.util.UUID

interface ITeam {
    val id: UUID
    val name: String
    val director: String
    val coach: String
    val logoPath: String
    
    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}