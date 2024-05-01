package model

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
        TODO("Not yet implemented")
    }

    override fun toXML(): String {
        TODO("Not yet implemented")
    }

    override fun toJSON(): String {
        TODO("Not yet implemented")
    }

}
