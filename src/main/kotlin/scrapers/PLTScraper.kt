package scrapers

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import it.skrape.selects.html5.iframe
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

    fun getTeams(
        teamMap: Map<String, String> = getTeamMap(LocalDate.now().year),
        downloadLogo: Boolean = false,
        saveToFile: Boolean = false
    ): Teams {
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
                if (downloadLogo) {
                    ImageUtil.downloadImage(imageFetchUrl, logoPath)
                }

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
                                            logoPath = if (downloadLogo) logoPath else imageFetchUrl
                                        )
                                    )
                                }

                            }
                        }
                    }
                }
            }
            if (saveToFile) teams.writeToJSON("src/main/resources/football_teams.json")
            println("Teams fetched successfully")
            return teams
        } catch (e: Exception) {
            println("Error fetching teams: ${e.message}")
            return teams
        }
    }

    fun getStandings(season: Int = LocalDate.now().year, teams: Teams = getTeams()): Standings {
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
                                    try {


                                        standings.add(
                                            DrawableStanding(
                                                place = place,
                                                team = teams.find { it.name == team }?.id
                                                    ?: throw Exception("Team not found"),
                                                gamesPlayed = gamesPlayed,
                                                wins = wins,
                                                draws = draws,
                                                losses = losses,
                                                goalsScored = goalStats[0],
                                                goalsConceded = goalStats[1],
                                                points = points
                                            )
                                        )
                                    } catch (e: Exception) {
                                        println("Error fetching standings: ${e.message}")
                                    }
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

    fun getStadiums(
        teamMap: Map<String, String> = getTeamMap(LocalDate.now().year),
        teams: Teams = getTeams(),
        downloadImage: Boolean = false
    ): Stadiums {
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
                                    var stadiumPath = ""
                                    div {
                                        withClass = "col-md-9.col-xs-12"
                                        
                                        imageFetchUrl += findFirst("img").attribute("src").trimStart('.')
                                            .replace(" ", "%20")
                                        stadiumPath = imageFetchUrl
                                        if (downloadImage) {
                                            stadiumPath = "src/main/resources/stadium_images/${name}_stadium.png"
                                            ImageUtil.downloadImage(imageFetchUrl, stadiumPath)
                                        }
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
                                            teamId = teams.find { it.name == name }?.id
                                                ?: throw Exception("Team not found"),
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
            println("Stadiums fetched successfully!")
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

    fun getMatches(
        teamsUrl: String = "https://www.prvaliga.si/tekmovanja/default.asp?id_menu=101&id_sezone=${LocalDate.now().year}",
        teams: Teams = getTeams(),
        stadiums: Stadiums = getStadiums(),
        cssSelector: String = "tr.klub_all"
    ): Matches {
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
                                val stadiumName = locationData?.get(1)?.trim() ?: ""
                                matches.add(
                                    Match(
                                        home = teams.find { it.name == columns[0].text }?.id
                                            ?: throw Exception("Team not found"),
                                        score = score,
                                        played = played,
                                        time = time,
                                        away = teams.find { it.name == columns[4].text }?.id
                                            ?: throw Exception("Team not found"),
                                        date = LocalDate.from(date),
                                        location = locationData[0].trim(),
                                        stadium = stadiums.find { it.name == stadiumName }?.id,
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
    
    fun saveAllData(fileType: FileType = FileType.JSON) {
        val teams = getTeams()
        val matches = getMatches(teams = teams)
        val stadiums = getStadiums(teams = teams)
        val standings = getStandings(teams = teams)
        when(fileType) {
            FileType.CSV -> {
                teams.writeToCSV("teams.csv")
                matches.writeToCSV("matches.csv")
                stadiums.writeToCSV("stadiums.csv")
                standings.writeToCSV("standings.csv")
            }
            FileType.XML -> {
                teams.writeToXML("teams.xml")
                matches.writeToXML("matches.xml")
                stadiums.writeToXML("stadiums.xml")
                standings.writeToXML("standings.xml")
            }
            FileType.JSON -> {
                teams.writeToJSON("teams.json")
                matches.writeToJSON("matches.json")
                stadiums.writeToJSON("stadiums.json")
                standings.writeToJSON("standings.json")
            }
        }
    }
}