package model

import interfaces.IStadium
import org.bson.types.ObjectId
import util.LocationUtil
import java.time.LocalDate

data class Stadium(
    override val name: String,
    override val teamId: ObjectId,
    override val capacity: UShort? = null,
    override var location: LocationUtil.Location? = null,
    override val buildYear: UShort? = null,
    override val imagePath: String? = null,
    override val season: UShort = LocalDate.now().year.toUShort(),
    override val id: ObjectId = ObjectId(),
) : IStadium {
    override fun toCSV(): String {
        return "$id;$name;$teamId;$capacity;$location;$buildYear;$imagePath;$season"
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
                <season>$season</season>
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
                ${if(capacity != null) {""" "capacity": "$capacity", """.trimIndent()} else ""}
                $location
                ${if(buildYear != null) {""" "buildYear": "$buildYear", """.trimIndent()} else ""}
                ${if(imagePath != null) {""" "imageUrl": "$imagePath", """.trimIndent()} else ""}  
                "season": "$season"
            },
        """.trimIndent()
    }

}