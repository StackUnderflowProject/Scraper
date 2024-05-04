package model

import interfaces.IStadium
import java.util.UUID

data class Stadium(
    override val name: String,
    override val teamId: UUID,
    override val capacity: UShort,
    override val location: String,
    override val buildYear: UShort,
    override val imagePath: String,
    override val id: UUID = UUID.randomUUID(),
) : IStadium {
    override fun toCSV(): String {
        return "$id;$name;$teamId;$capacity;$location;$buildYear;$imagePath"
    }

    override fun toXML(): String {
        return """
            <stadium id="$id">
                <name>$name</name>
                <teamId>$teamId</teamId>
                <capacity>$capacity</capacity>
                <location>$location</location>
                <buildYear>$buildYear</buildYear>
                <imageUrl>$imagePath</imageUrl>
            </stadium>
        """.trimIndent()
    }

    override fun toJSON(): String {
        return """
            {
                "id": "$id",
                "name": "$name",
                "teamId": "$teamId",
                "capacity": "$capacity",
                "location": "$location",
                "buildYear": "$buildYear",
                "imageUrl": "$imagePath"
            },
        """.trimIndent()
    }

}
