package model

import interfaces.IStadium
import org.bson.types.ObjectId
import util.LocationUtil
import java.util.UUID

data class Stadium(
    override val name: String,
    override val teamId: ObjectId,
    override val capacity: UShort? = null,
    override var location: LocationUtil.Location? = null,
    override val buildYear: UShort? = null,
    override val imagePath: String? = null,
    override val id: ObjectId = ObjectId(),
) : IStadium {
    override fun toCSV(): String {
        return "$id;$name;$teamId;$capacity;$location;$buildYear;$imagePath"
    }

    override fun toXML(): String {
        val location = if (location != null) """
            <location>
                <lat>${location!!.lat}</lat>
                <lng>${location!!.lng}</lng>
            </location>
        """.trimIndent() else ""
        return """
            <stadium id="$id">
                <name>$name</name>
                <teamId>$teamId</teamId>
                <capacity>$capacity</capacity>
                $location
                <buildYear>$buildYear</buildYear>
                <imageUrl>$imagePath</imageUrl>
            </stadium>
        """.trimIndent()
    }

    override fun toJSON(): String {
        val location = if (location != null) """
            "location": {
                "type": "Point",
                "coordinates": [${location!!.lat}, ${location!!.lng}]
            },
        """.trimIndent() else ""
        return """
            {
                "id": "$id",
                "name": "$name",
                "teamId": "$teamId",
                "capacity": "$capacity",
                $location
                "buildYear": "$buildYear",
                "imageUrl": "$imagePath"
            },
        """.trimIndent()
    }

}
