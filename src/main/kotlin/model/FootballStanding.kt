package model

import java.util.*

data class FootballStanding (
    override val place: UShort,
    override val team: String,
    override val gamesPlayed: UShort,
    override val wins: UShort,
    val draws: UShort,
    override val losses: UShort,
    override val goalsScored: UShort,
    override val goalsConceded: UShort,
    override val points: UShort,
    override val id: UUID = UUID.randomUUID()
): IStanding {
    
    private val goalDiff = (goalsScored - goalsConceded).toShort()
    
    override fun toCSV(): String {
        return "$place;$team;$gamesPlayed;$wins;$draws;$losses;$goalsScored;$goalsConceded;$goalDiff;$points"
    }
    
    override fun toXML(): String {
        return """
            <standing id="$id">
                <place>$place</place>
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