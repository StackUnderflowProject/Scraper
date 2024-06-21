package model

import interfaces.ITeam
import org.bson.types.ObjectId
import java.time.LocalDate

class HandballTeam(
    override val id: ObjectId = ObjectId(),
    override val name: String,
    override val director: String,
    val president: String,
    override var coach: String,
    override var logoPath: String = "src/main/resources/default_logo.png",
    override val season: UShort = LocalDate.now().year.toUShort()
) : ITeam {
    override fun toCSV(): String {
        return "$id;$name;$director;$president;$coach;$logoPath;$season"
    }

    override fun toXML(): String {
        return """
            <team id="$id">
                <name>$name</name>
                <director>$director</director>
                <president>$president</president>
                <coach>$coach</coach>
                <logoPath>$logoPath</logoPath>
                <season>$season</season>
            </team>
        """.trimIndent()
    }

    override fun toJSON(): String {
        return """
            {
                "id": "$id",
                "name": "$name",
                "director": "$director",
                "president": "$president",
                "coach": "$coach",
                "logoPath": "$logoPath",
                "season": "$season"
            },
        """.trimIndent()
    }

}