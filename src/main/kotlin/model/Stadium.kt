package model

import interfaces.IStadium
import org.bson.types.ObjectId
import java.util.UUID

data class Stadium(
    override val name: String,
    override val teamId: ObjectId,
    override val capacity: UShort? = null,
    override val location: String? = null,
    override val buildYear: UShort? = null,
    override val imagePath: String? = null,
    override val id: ObjectId = ObjectId(),
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
