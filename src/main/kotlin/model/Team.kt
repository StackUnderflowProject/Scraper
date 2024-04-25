package model

import java.util.UUID

data class Team(
    override val name: String,
    override val president: String,
    override val director: String,
    override val coach: String,
    override val logoPath: String,
    override val id: UUID = UUID.randomUUID(),
): ITeam {

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
                "coach": "$coach"
                "logoPath": "$logoPath"
            },
        """.trimIndent()
    }

}
