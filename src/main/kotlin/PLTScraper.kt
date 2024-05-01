import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import model.*
import util.ImageUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

object PLTScraper {
    fun getTeamMap(season: Int): Map<String, String> {
        val idPattern = Regex(""".*id_kluba=(\d+).*""")
        val teamMap = mutableMapOf<String, String>()

        val teamsUrl = "https://www.prvaliga.si/tekmovanja/default.asp?action=lestvica&id_menu=102&id_sezone=$season"
        skrape(HttpFetcher) {
            request {
                url = teamsUrl
            }

            response {
                htmlDocument {
                    table {
                        tbody {
                            val rows = findAll("tr")
                            rows.forEach { row ->
                                val link = row.attribute("onclick")
                                if (idPattern.matches(link)) {
                                    val idMatch = idPattern.find(link) ?: return@forEach
                                    val name = row.findAll("td")[3].text
                                    val (id) = idMatch.destructured
                                    teamMap[name] = id
                                }
                            }
                        }
                    }
                }
            }
        }
        return teamMap
    }

    fun getTeams(teamMap: Map<String, String>): Teams {
        val baseUrl = "https://www.prvaliga.si/tekmovanja/default.asp?action=klub&id_menu=217&id_kluba="
        val teams = Teams()

        println("Fetching teams...")
        try {
            teamMap.forEach { entry ->
                val (name, id) = entry
                val teamUrl = "$baseUrl$id"
                var imageFetchUrl = "https://www.prvaliga.si"
                

                skrape(HttpFetcher) {
                    request {
                        url = teamUrl
                    }
                    response {
                        htmlDocument {
                            div {
                                withClass = "col-md-4.col-xs-12.text-center"
                                imageFetchUrl += findFirst("img").attribute("src").trimStart('.').replace(" ", "%20")
                            }
                        }
                    }
                }

                val logoPath = "src/main/resources/football_team_logos/${name}_logo.png"
                ImageUtil.downloadImage(imageFetchUrl, logoPath)

                skrape(HttpFetcher) {
                    request {
                        url = teamUrl
                    }

                    response {
                        htmlDocument {
                            table {
                                tbody {
                                    val rows = findAll("tr")
                                    val president = rows[1].findFirst("td").text.split(":")[1].trim()
                                    val director = rows[2].findFirst("td").text.split(":")[1].trim()

                                    val coachRow = rows.find { it.text.contains("Glavni trener") }
                                    val coach = coachRow?.findFirst("td")?.text?.split(":")?.get(1)?.trim() ?: ""



                                    teams.add(
                                        FootballTeam(
                                            name = name,
                                            president = president,
                                            director = director,
                                            coach = coach,
                                            logoPath = logoPath
                                        )
                                    )
                                }

                            }
                        }
                    }
                }
            }
            println("Teams fetched successfully")
            return teams
        } catch (e: Exception) {
            println("Error fetching teams: ${e.message}")
            return teams
        }
    }

    fun getStandings(season: Int): Standings {
        println("Getting standings...")
        val searchURL = "https://www.prvaliga.si/tekmovanja/default.asp?action=lestvica&id_menu=102&id_sezone=$season"
        val standings = Standings()
        skrape(HttpFetcher) {
            request {
                url = searchURL
            }

            response {
                htmlDocument {
                    table {
                        tbody {
                            val rows = findAll("tr")
                            rows.map { row ->
                                val columns = row.findAll("td")
                                if (columns.size == 11) {
                                    val place = columns[0].text.removeSuffix(".").toUShort()
                                    val team = columns[3].text.trim()
                                    val gamesPlayed = columns[4].text.toUShort()
                                    val wins = columns[5].text.toUShort()
                                    val draws = columns[6].text.toUShort()
                                    val losses = columns[7].text.toUShort()
                                    val goalStats = columns[8].text.split(":").map { it.toUShort() }.toTypedArray()
                                    val goalDiff = columns[9].text
                                    val points = columns[10].text.toUShort()
                                    standings.add(
                                        FootballStanding(
                                            place = place,
                                            team = team,
                                            gamesPlayed = gamesPlayed,
                                            wins = wins,
                                            draws = draws,
                                            losses = losses,
                                            goalsScored = goalStats[0],
                                            goalsConceded = goalStats[1],
                                            points = points
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        println("Standings fetched successfully!")
        return standings
    }

    fun getStadiums(teamMap: Map<String, String>): Stadiums {
        val baseUrl = "https://www.prvaliga.si/tekmovanja/default.asp?action=klub&id_menu=217&id_kluba="
        val viewUrl = "&prikaz=3"
        val stadiums = Stadiums()
        println("Getting stadiums...")
        try {
            teamMap.forEach { entry ->
                val (name, id) = entry
                val stadiumUrl = "$baseUrl$id$viewUrl"
                var imageFetchUrl = "https://www.prvaliga.si"

                skrape(HttpFetcher) {
                    request {
                        url = stadiumUrl
                    }
                    response {
                        htmlDocument {
                            div {
                                withClass = "row"
                                div {
                                    withClass = "row"
                                    var stadiumName = ""
                                    var buildYear = 0.toUShort()
                                    var capacity = 0.toUShort()
                                    var location = ""
                                    val stadiumPath = "src/main/resources/football_stadiums/${name}_stadium.png"
                                    div {
                                        withClass = "col-md-9.col-xs-12"
                                        imageFetchUrl += findFirst("img").attribute("src").trimStart('.').replace(" ", "%20")
                                        ImageUtil.downloadImage(imageFetchUrl, stadiumPath)
                                        table {
                                            tbody {
                                                val rows = findAll("tr")
                                                stadiumName = try {
                                                    rows[0].findAll("th")[1].text
                                                } catch (e: Exception) {
                                                    rows[0].findAll("td")[1].text
                                                }
                                                buildYear = rows[1].findAll("td")[1].text.toUShort()
                                                capacity = rows[2].findAll("td")[1].text.split(' ')[0].replace(".", "")
                                                    .toUShort()
                                            }
                                        }
                                    }
                                    div {
                                        withClass = "col-md-3.col-xs-12"
                                        table {
                                            tbody {
                                                location = findAll("tr")[1].findFirst("td").text.split("tel:")[0].trim()
                                            }
                                        }
                                    }
                                    stadiums.add(
                                        Stadium(
                                            name = stadiumName,
                                            capacity = capacity,
                                            location = location,
                                            buildYear = buildYear,
                                            imagePath = stadiumPath
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

            }
            return stadiums
        } catch (e: Exception) {
            println("Error fetching stadiums: ${e.message}")
            return stadiums
        }
    }

    @Throws(IllegalArgumentException::class)
    fun getTeam(teamMap: Map<String, String>): String {
        for (key in teamMap.keys) {
            println(key)
        }
        print("Enter team name (empty for all teams): ")
        var team = readln().replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
        if (team.isEmpty()) return ""

        while (teamMap.containsKey(team).not()) {
            print("Team not found, try again: ")
            team = readln().replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
        }
        return teamMap[team] ?: throw IllegalArgumentException("Team not found")
    }

    fun getCssSelector(teamId: String, matchTypeArg: String? = null): String {
        var matchType = matchTypeArg ?: ""
        if (matchType.isEmpty()) {
            println("model.Match type:\n1. Played\n2. Upcoming\n3. All")
            matchType = when (readln().toIntOrNull()) {
                1 -> "played"
                2 -> "upcoming"
                else -> "all"
            }
        }

        val clubId = if (teamId.isEmpty()) "" else ".klub_$teamId"
        var cssSelector = "tr.hidden-xs.klub_all$clubId"
        when (matchType) {
            "played" -> {
                cssSelector += ".odigrano"
            }

            "upcoming" -> {
                cssSelector += ".neodigrano"
            }

            else -> {
            }
        }
        return cssSelector
    }

    @Throws(IllegalArgumentException::class)
    fun getOutputType(): FileType {
        println("Output type:\n1. CSV\n2. XML\n3. JSON")
        var outputType = readln().toIntOrNull() ?: 0
        while (outputType !in 1..3) {
            println("Invalid output type, try again: ")
            outputType = readln().toIntOrNull() ?: 0
        }
        return when (outputType) {
            1 -> FileType.CSV
            2 -> FileType.XML
            3 -> FileType.JSON
            else -> throw IllegalArgumentException("Invalid output type")
        }
    }

    fun getSeason(): Int {
        var season = 0
        while (season !in 1992..2024) {
            println("Enter season (1992-2024, empty current season): ")
            season = readln().toIntOrNull() ?: LocalDate.now().year
        }
        return season
    }

    fun getMatches(teamsUrl: String, cssSelector: String): Matches {
        println("Getting matches...")
        val dateFormatter =
            DateTimeFormatter.ofPattern("EEEE, d.MMMM yyyy", Locale.Builder().setLanguage("sl").setRegion("SI").build())
        val matches = Matches()
        skrape(HttpFetcher) {
            request {
                url = teamsUrl
            }

            response {
                htmlDocument {
                    table {
                        withClass = "tekme"
                        tbody {
                            val rows = findAll(cssSelector = cssSelector)
                            rows.map { row ->
                                val columns = row.findAll("td")
                                val geoData = columns[5].text.split('|')
                                val date = dateFormatter.parse(geoData[0].trim().lowercase())
                                val locationData = geoData[1].trim().split(',')
                                val scoreTimeData = columns[2].text.split(':')
                                val played = scoreTimeData[0].trim().length == 1;
                                val score = if (played) columns[2].text else null
                                val time = if (!played) columns[2].text else null
                                matches.add(
                                    Match(
                                        home = columns[0].text,
                                        score = score,
                                        played = played,
                                        time = time,
                                        away = columns[4].text,
                                        date = LocalDate.from(date),
                                        location = locationData[0].trim(),
                                        stadium = if (locationData.size == 2) locationData[1].trim() else "",
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        println("Matches fetched successfully!")
        return matches
    }

    fun parseArgs(args: Array<String>): Map<String, String> {
        val argMap = mutableMapOf<String, String>()
        args.forEach {
            val parts = it.split("=")
            if (parts.size == 2) {
                val key = parts[0].trimStart('-')
                val value = parts[1].trim('\"')
                validateArg(key, value)
                argMap[key] = value
            }
        }
        return argMap
    }

    private fun validateArg(key: String, value: String) {
        when (key) {
            "season" -> {
                val season = value.toIntOrNull()
                if (season == null || season !in 1992..2024) {
                    throw IllegalArgumentException("Invalid season: $value. Please provide a season between 1992 and 2024.")
                }
            }

            "output" -> {
                if (value !in listOf("csv", "xml", "json")) {
                    throw IllegalArgumentException("Invalid output type: $value. Please provide one of csv, xml, or json.")
                }
            }

            "standings", "matches", "interactive" -> {
                if (value !in listOf("true", "false")) {
                    throw IllegalArgumentException("Invalid value for $key: $value. Please provide true or false.")
                }
            }

            "match-type" -> {
                if (value !in listOf("played", "upcoming", "all")) {
                    throw IllegalArgumentException("Invalid match type: $value. Please provide one of played, upcoming, or all.")
                }
            }

            "team" -> {
                if (value.first().isLowerCase()) {
                    throw IllegalArgumentException("Invalid team name: $value. Please provide a team name starting with an uppercase letter.")
                }
            }
        }
    }

    fun checkHelp(args: Array<String>) {
        if (args.contains("--help") || args.isEmpty()) {
            println("Scrape PrvaLiga Telekom Slovenije data.")
            println("Usage: plt-scraper [OPTION]...")
            println("Options:")
            println("  --help\t\t\t\tDisplay this help message")
            println("  --interactive=BOOL\tRun the scraper in interactive mode, default is false")
            println("  --season=YEAR\t\t\tSet the season (1992-2024), default is current season")
            println("  --output=TYPE\t\t\tSet the output type (csv, xml, json), default is json")
            println("  --standings=BOOL\t\tSet whether to get standings (true, false), default is true")
            println("  --matches=BOOL\t\tSet whether to get matches (true, false), default is false")
            println("  --team=TEAM\t\t\tSet the team name (empty for all teams), default is empty")
            println("  --match-type=TYPE\t\tSet the match type (played, upcoming, all), default is all")
            println("Examples:")
            println("\tplt-scraper --season=2022 --output=xml --standings=true --matches=true --team=Olimpija --match-type=all")
            exitProcess(0)
        }
    }

    fun setDefaultArgs(args: MutableMap<String, String>) {
        if (args.containsKey("interactive").not()) {
            args["interactive"] = "false"
        }

        if (args.containsKey("season").not()) {
            args["season"] = LocalDate.now().year.toString()
        }

        if (args.containsKey("output").not()) {
            args["output"] = FileType.JSON.value
        }

        if (args.containsKey("standings").not()) {
            args["standings"] = "true"
        }

        if (args.containsKey("matches").not()) {
            args["matches"] = "false"
        }

        if (args.containsKey("team").not()) {
            args["team"] = ""
        }

        if (args.containsKey("match-type").not()) {
            args["match-type"] = "all"
        }
    }

}