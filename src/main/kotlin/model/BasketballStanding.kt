package model

import interfaces.IStanding
import org.bson.types.ObjectId
import java.time.LocalDate

data class BasketballStanding(
    override val place: UShort,
    override val gamesPlayed: UShort,
    override val wins: UShort,
    override val losses: UShort,
    override val points: UShort,
    override val goalsScored: UShort,
    override val goalsConceded: UShort,
    override val team: ObjectId = ObjectId(),
    override val id: ObjectId = ObjectId(),
    override val season: UShort = LocalDate.now().year.toUShort()
) : IStanding {
    private val goalDiff = (goalsScored - goalsConceded).toShort()

    override fun toCSV(): String {
        return "$place;$season;$team;$gamesPlayed;$wins;$losses;$points;$goalsScored;$goalsConceded;$goalDiff"
    }

    override fun toXML(): String {
        return """
            <standing id="$id">
                <place>$place</place>
                <season>$season</season>
                <team>$team</team>
                <gamesPlayed>$gamesPlayed</gamesPlayed>
                <wins>$wins</wins>
                <losses>$losses</losses>
                <points>$points</points>
                <goalsScored>$goalsScored</goalsScored>
                <goalsConceded>$goalsConceded</goalsConceded>
                <goalDiff>$goalDiff</goalDiff>
            </standing>
        """.trimIndent()
    }

    override fun toJSON(): String {
        return """
              {
                "id": "$id",
                "season": "$season",
                "place": "$place",
                "team": "$team",
                "gamesPlayed": "$gamesPlayed",
                "wins": "$wins",
                "losses": "$losses",
                "points": "$points",
                "goalsScored": "$goalsScored",
                "goalsConceded": "$goalsConceded",
                "goalDiff": "$goalDiff"
            },
        """.trimIndent()
    }

}