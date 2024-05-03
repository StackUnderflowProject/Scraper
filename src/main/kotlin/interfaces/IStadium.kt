package interfaces

import java.util.*

interface IStadium {
    val name: String
    val capacity: UShort
    val location: String
    val buildYear: UShort
    val imagePath: String
    val id: UUID

    fun toCSV(): String
    fun toXML(): String
    fun toJSON(): String
}