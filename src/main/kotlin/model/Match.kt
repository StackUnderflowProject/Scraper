package model

import interfaces.IMatch
import org.bson.types.ObjectId
import java.time.LocalDate
import java.util.UUID

data class Match(
    override val home: ObjectId,
    override val score: String?,
    override val time: String?,
    override val away: ObjectId,
    override val played: Boolean = false,
    override val date: LocalDate,
    override val location: String,
    val stadium: ObjectId? = null,
    override val id: ObjectId = ObjectId()
) : IMatch {
    override fun toString(): String {
        return when (played) {
            true -> "$date -> $home vs $away $score @ $location, $stadium"
            false -> "$date $time -> $home vs $away @ $location, $stadium"
        }
    }

    override fun toCSV(): String {
        return "$date;${time ?: ""};$home;$away;${score ?: ""};$location;$stadium"
    }

    override fun toXML(): String {
        return """
            <match id="$id">
                <date>$date</date>
                <time>${time ?: ""}</time>
                <home>$home</home>
                <away>$away</away>
                <score>${score ?: ""}</score>
                <location>$location</location>
                <stadium>$stadium</stadium>
            </match>
        """.trimIndent()
    }

    override fun toJSON(): String {
        return """
              {
                "id": "$id",
                "date": "$date",
                "time": "${time ?: ""}",
                "home": "$home",
                "away": "$away",
                "score": "${score ?: ""}",
                "location": "$location",
                "stadium": "$stadium"
            },
        """.trimIndent()
    }
}