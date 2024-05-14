package model

import interfaces.ITeam
import org.bson.types.ObjectId

data class FootballTeam(
    override val name: String,
    val president: String,
    override val director: String,
    override var coach: String,
    override var logoPath: String,
    override val id: ObjectId = ObjectId(),
) : ITeam {

    override fun toString(): String {
        return "Team(id=$id, name='$name', president='$president', director='$director', coach='$coach', logoPath='$logoPath')"
    }

    override fun toCSV(): String {
        return "$id;$name;$president;$director;$coach;$logoPath"
    }

    override fun toXML(): String {
        return """
            <team id="$id">
                <name>$name</name>
                <president>$president</president>
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
                "president": "$president",
                "director": "$director",
                "coach": "$coach",
                "logoPath": "$logoPath"
            },
        """.trimIndent()
    }

}