package model

import interfaces.IStanding
import org.bson.types.ObjectId
import java.time.LocalDate

data class DrawableStanding(
    override val place: UShort,
    override val team: ObjectId,
    override val gamesPlayed: UShort,
    override val wins: UShort,
    val draws: UShort,
    override val losses: UShort,
    override val goalsScored: UShort,
    override val goalsConceded: UShort,
    override val points: UShort,
    override val season: UShort = LocalDate.now().year.toUShort(),
    override val id: ObjectId = ObjectId()
) : IStanding {

    private val goalDiff = (goalsScored - goalsConceded).toShort()

    override fun toCSV(): String {
        return "$place;$season;$team;$gamesPlayed;$wins;$draws;$losses;$goalsScored;$goalsConceded;$goalDiff;$points"
    }

    override fun toXML(): String {
        return """
            <standing id="$id">
                <place>$place</place>
                <season>$season</season>
                <team>$team</team>
                <gamesPlayed>$gamesPlayed</gamesPlayed>
                <wins>$wins</wins>
                <draws>$draws</draws>
                <losses>$losses</losses>
                <goalsScored>$goalsScored</goalsScored>
                <goalsConceded>$goalsConceded</goalsConceded>
                <goalDiff>$goalDiff</goalDiff>
                <points>$points</points>
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
                "draws": "$draws",
                "losses": "$losses",
                "goalsScored": "$goalsScored",
                "goalsConceded": "$goalsConceded",
                "goalDiff": "$goalDiff",
                "points": "$points"
            },
        """.trimIndent()
    }
}