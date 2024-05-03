package model

import interfaces.IStanding
import java.util.*

data class BasketballStanding(
    override val place: UShort,
    override val team: String,
    override val gamesPlayed: UShort,
    override val wins: UShort,
    override val losses: UShort,
    override val points: UShort,
    override val goalsScored: UShort,
    override val goalsConceded: UShort,
    val homeWins: UShort,
    val homeLosses: UShort,
    val awayWins: UShort,
    val awayLosses: UShort,
    override val id: UUID
) : IStanding {
    val goalDiff = (goalsScored - goalsConceded).toShort()
    
    
    override fun toCSV(): String {
        return "$place;$team;$gamesPlayed;$wins;$losses;$points;$goalsScored;$goalsConceded;$homeWins;$homeLosses;$awayWins;$awayLosses"
    }

    override fun toXML(): String {
        return """
            <standing id="$id">
                <place>$place</place>
                <team>$team</team>
                <gamesPlayed>$gamesPlayed</gamesPlayed>
                <wins>$wins</wins>
                <losses>$losses</losses>
                <points>$points</points>
                <goalsScored>$goalsScored</goalsScored>
                <goalsConceded>$goalsConceded</goalsConceded>
                <homeWins>$homeWins</homeWins>
                <homeLosses>$homeLosses</homeLosses>
                <awayWins>$awayWins</awayWins>
                <awayLosses>$awayLosses</awayLosses>
            </standing>
        """.trimIndent()
    }

    override fun toJSON(): String {
        return """
              {
                "id": "$id",
                "place": "$place",
                "team": "$team",
                "gamesPlayed": "$gamesPlayed",
                "wins": "$wins",
                "losses": "$losses",
                "points": "$points",
                "goalsScored": "$goalsScored",
                "goalsConceded": "$goalsConceded",
                "homeWins": "$homeWins",
                "homeLosses": "$homeLosses",
                "awayWins": "$awayWins",
                "awayLosses": "$awayLosses"
            },
        """.trimIndent()
    }

}
