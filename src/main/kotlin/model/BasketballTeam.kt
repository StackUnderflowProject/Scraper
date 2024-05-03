package model

import interfaces.ITeam
import java.util.*

data class BasketballTeam(
    override val id: UUID = UUID.randomUUID(),
    override val name: String,
    override val director: String,
    override val coach: String,
    override var logoPath: String
): ITeam {
    override fun toCSV(): String {
        return "$name;$director;$coach;$logoPath"
    }

    override fun toXML(): String {
        return """
        <team id="$id">
            <name>$name</name>
            <director>$director</director>
            <coach>$coach</coach>
            <logoPath>$logoPath</logoPath>
        </team>
        """.trimIndent()
    }

    override fun toJSON(): String {
        return """
        {
            "id": "$id",
            "name": "$name",
            "director": "$director",
            "coach": "$coach",
            "logoPath": "$logoPath"
        },
        """.trimIndent()
    }

}