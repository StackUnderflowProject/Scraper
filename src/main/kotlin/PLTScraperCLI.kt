import it.skrape.selects.html5.*
import model.*
import kotlin.system.exitProcess


@Throws(IllegalArgumentException::class)
fun main(argv: Array<String>) {
    PLTScraper.checkHelp(argv)
    try {
        val args = PLTScraper.parseArgs(argv).toMutableMap()
        PLTScraper.setDefaultArgs(args)

        if (args["interactive"].toBoolean()) {
            val season = PLTScraper.getSeason()

            println("Do you want to get matches? yes/no")
            val wantsMatches = readln().lowercase() == "yes"

            println("Do you want to get standings? yes/no")
            val wantsStandings = readln().lowercase() == "yes"

            val outputType = PLTScraper.getOutputType()

            if (wantsMatches) {
                val teamMap = PLTScraper.getTeamMap(season)
                val teamId = PLTScraper.getTeam(teamMap)
                val cssSelector = PLTScraper.getCssSelector(teamId)
                val teamsUrl = "https://www.prvaliga.si/tekmovanja/default.asp?id_menu=101&id_sezone=$season"
                val matches = PLTScraper.getMatches(teamsUrl, cssSelector)

                when (outputType) {
                    FileType.CSV -> matches.writeToCSV()
                    FileType.XML -> matches.writeToXML()
                    FileType.JSON -> matches.writeToJSON()
                }
            }

            if (wantsStandings) {
                val standings = PLTScraper.getStandings(season)
                when (outputType) {
                    FileType.CSV -> standings.writeToCSV()
                    FileType.XML -> standings.writeToXML()
                    FileType.JSON -> standings.writeToJSON()
                }
            }
            return
        }

        if (args["standings"].toBoolean()) {
            val standings = PLTScraper.getStandings(args["season"]!!.toInt())
            when (FileType.valueOf(args["output"]!!.uppercase())) {
                FileType.CSV -> standings.writeToCSV()
                FileType.XML -> standings.writeToXML()
                FileType.JSON -> standings.writeToJSON()
            }
        }

        if (args["matches"].toBoolean()) {
            val teamMap = PLTScraper.getTeamMap(args["season"]!!.toInt())
            val teamId = teamMap[args["team"]!!] ?: ""
            val cssSelector = PLTScraper.getCssSelector(teamId, args["match-type"]!!)
            val teamsUrl = "https://www.prvaliga.si/tekmovanja/default.asp?id_menu=101&id_sezone=${args["season"]}"
            val matches = PLTScraper.getMatches(teamsUrl, cssSelector)

            when (FileType.valueOf(args["output"]!!.uppercase())) {
                FileType.CSV -> matches.writeToCSV()
                FileType.XML -> matches.writeToXML()
                FileType.JSON -> matches.writeToJSON()
            }
        }

    } catch (e: IllegalArgumentException) {
        println(e.message)
        return
    }
}

