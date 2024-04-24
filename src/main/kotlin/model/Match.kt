package model

import java.time.LocalDate
import java.util.UUID

data class Match (
    val home: String,
    val score: String?,
    val time: String?,
    val away: String,
    val played: Boolean = false,
    val date: LocalDate,
    val location: String,
    val stadium: String,
    val id: UUID = UUID.randomUUID()
) {
    override fun toString(): String {
        return when(played) {
            true -> "$date -> $home vs $away $score @ $location, $stadium"
            false -> "$date $time -> $home vs $away @ $location, $stadium"
        }
    }
    
    fun toCSV(): String {
        return "$date;${time ?: ""};$home;$away;${score ?: ""};$location;$stadium"
    }
    
    fun toXML(): String {
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
    
    fun toJSON(): String {
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