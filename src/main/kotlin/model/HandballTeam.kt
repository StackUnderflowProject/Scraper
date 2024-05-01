package model

import java.util.*

class HandballTeam(
    override val id: UUID = UUID.randomUUID(),
    override val name: String,
    override val director: String,
    val president: String,
    override val coach: String,
    override var logoPath: String = "src/main/resources/default_logo.png"
): ITeam {
    override fun toCSV(): String {
        TODO("Not yet implemented")
    }

    override fun toXML(): String {
        TODO("Not yet implemented")
    }

    override fun toJSON(): String {
        TODO("Not yet implemented")
    }

}