package model

import interfaces.ITeam
import org.bson.types.ObjectId

class HandballTeam(
    override val id: ObjectId = ObjectId(),
    override val name: String,
    override val director: String,
    val president: String,
    override var coach: String,
    override var logoPath: String = "src/main/resources/default_logo.png"
) : ITeam {
    override fun toCSV(): String {
        return "$id;$name;$director;$president;$coach;$logoPath"
    }

    override fun toXML(): String {
        return """
            <team id="$id">
                <name>$name</name>
                <director>$director</director>
                <president>$president</president>
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
                "president": "$president",
                "coach": "$coach",
                "logoPath": "$logoPath"
            },
        """.trimIndent()
    }

}