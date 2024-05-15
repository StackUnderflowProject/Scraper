package scrapers

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import model.*
import util.ImageUtil
import util.LocationUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object PLTScraper {

    /**
     * Fetches a map of team names to their corresponding IDs on the website.
     *
     * @param season An UShort representing the season year. If not provided, it defaults to the current year.
     * @return A map of team names to their corresponding IDs on the website.
     */
    private fun getTeamMap(season: UShort): Map<String, String> {
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

    /**
     * Fetches team data from the website.
     *
     * @param season An UShort representing the season year. If not provided, it defaults to the current year.
     * @param teamMap A map of team names to their corresponding IDs on the website. If not provided, it defaults to the current year's teams.
     * @param downloadLogo A boolean indicating whether to download the team's logo or not. If true, the logo will be downloaded and saved locally. If false, the logo's URL will be saved.
     * @return A Teams object containing all the fetched teams.
     */
    fun getTeams(
        season: UShort = LocalDate.now().year.toUShort(),
        teamMap: Map<String, String> = getTeamMap(LocalDate.now().year.toUShort()),
        downloadLogo: Boolean = false,
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
                                            logoPath = if (downloadLogo) logoPath else imageFetchUrl,
                                            season = season
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

    /**
     * Fetches the standings data from the website.
     *
     * @param season An UShort representing the season year. If not provided, it defaults to the current year.
     * @param teams A Teams object containing all the teams. If not provided, it defaults to the result of the getTeams() function.
     * @return A Standings object containing all the fetched standings.
     */
    fun getStandings(season: UShort = LocalDate.now().year.toUShort(), teams: Teams = getTeams()): Standings {
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
                                                points = points,
                                                season = season
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

    /**
     * Fetches stadium data from the website.
     *
     * @param season An UShort representing the season year. If not provided, it defaults to the current year.
     * @param teamMap A map of team names to their corresponding IDs on the website. If not provided, it defaults to the current year's teams.
     * @param teams A Teams object containing all the teams. If not provided, it defaults to the result of the getTeams() function.
     * @param downloadImage A boolean indicating whether to download the stadium's image or not. If true, the image will be downloaded and saved locally. If false, the image's URL will be saved.
     * @return A Stadiums object containing all the fetched stadiums.
     */
    fun getStadiums(
        season: UShort = LocalDate.now().year.toUShort(),
        teamMap: Map<String, String> = getTeamMap(LocalDate.now().year.toUShort()),
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
                                                capacity = rows.find { it.text.contains("Kapaciteta") }
                                                    ?.findAll("td")?.get(1)?.text
                                                    ?.split(" ")?.get(0)
                                                    ?.replace(".", "")
                                                    ?.toUShort() ?: 0.toUShort()
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
                                            location = LocationUtil.getLocation(location),
                                            buildYear = buildYear,
                                            imagePath = stadiumPath,
                                            season = season
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

    /**
     * Fetches match data from the website.
     *
     * @param season An UShort representing the season year. If not provided, it defaults to the current year.
     * @param teams A Teams object containing all the teams. If not provided, it defaults to the result of the getTeams() function.
     * @param stadiums A Stadiums object containing all the stadiums. If not provided, it defaults to the result of the getStadiums() function.
     * @return A Matches object containing all the fetched matches.
     */
    fun getMatches(
        season: UShort = LocalDate.now().year.toUShort(),
        teams: Teams = getTeams(),
        stadiums: Stadiums = getStadiums(),
    ): Matches {
        val teamsUrl = "https://www.prvaliga.si/tekmovanja/default.asp?id_menu=101&id_sezone=${season}"
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
                            val rows = findAll("tr.klub_all")
                            rows.map { row ->
                                val columns = row.findAll("td")
                                val geoData = columns[5].text.split('|')
                                val date = dateFormatter.parse(geoData[0].trim().lowercase())
                                val locationData = geoData[1].trim().split(',')
                                val scoreTimeData = columns[2].text.split(':')
                                val played = scoreTimeData[0].trim().length == 1
                                val score = if (played) columns[2].text else null
                                val time = if (!played) columns[2].text else null
                                val home = teams.find { it.name == columns[0].text }?.id
                                    ?: throw Exception("Team not found")
                                val away = teams.find { it.name == columns[4].text }?.id
                                    ?: throw Exception("Team not found")
                                matches.add(
                                    Match(
                                        home = home,
                                        score = score,
                                        played = played,
                                        time = time,
                                        away = away,
                                        date = LocalDate.from(date),
                                        location = locationData[0].trim(),
                                        stadium = stadiums.find { it.teamId == home }?.id,
                                        season = season
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

    /**
     * Fetches all data (teams, stadiums, standings, matches) for a given season and saves it to files in a specified format.
     *
     * @param season An integer representing the season year. If not provided, it defaults to the current year.
     * @param fileType A FileType enum value indicating the format in which to save the data. If not provided, it defaults to JSON.
     *
     * The method performs the following steps:
     * 1. Fetches a map of team names to their corresponding IDs on the website for the given season.
     * 2. Fetches team data using the fetched team map.
     * 3. Fetches stadium data using the fetched team map and team data.
     * 4. Fetches standings data for the given season using the fetched team data.
     * 5. Fetches match data for the given season using the fetched team data and stadium data.
     * 6. Depending on the specified fileType, it saves the fetched data to CSV, XML, or JSON files.
     */
    fun saveAllData(season: UShort = LocalDate.now().year.toUShort(), fileType: FileType = FileType.JSON) {
        val teamMap = getTeamMap(season)
        val teams = getTeams(season = season, teamMap = teamMap)
        val stadiums = getStadiums(season = season, teamMap = teamMap, teams = teams)
        val standings = getStandings(season = season, teams = teams)
        val matches = getMatches(season = season, teams = teams, stadiums = stadiums)
        when (fileType) {
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