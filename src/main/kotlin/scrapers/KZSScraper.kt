package scrapers

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.div
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import model.*
import util.LocationUtil
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

object KZSScraper {

    /**
     * Fetches the geographical location (latitude and longitude) of a given address.
     *
     * @param address The address to geocode.
     * @return A Location object containing the latitude and longitude of the address, or null if the address could not be geocoded.
     */
    fun getTeamUrlMap(): Map<String, String> {
        val teamUrlMap: MutableMap<String, String> = mutableMapOf()
        val teamsUrl = "https://www.eurobasket.com/Slovenia/basketball-Liga-Nova-KBM-Teams.aspx"
        skrape(HttpFetcher) {
            request {
                url = teamsUrl
            }
            response {
                htmlDocument {
                    div {
                        withClass = "BasketBallTeamDetails"
                        val entries = findAll("div.BasketBallTeamDetailsLine")
                        entries.forEach { entry ->
                            val name = entry.findFirst("div.BasketBallTeamName").text
                            teamUrlMap[name] = entry.findFirst("div.BasketBallTeamName > a").attribute("href")
                        }
                    }
                }
            }
        }
        return teamUrlMap
    }

    /**
     * Fetches the teams from the website.
     *
     * @param teamUrlMap A map of team names to their corresponding URLs on the website. If not provided, it defaults to the URLs fetched by getTeamMapUrl().
     * @return A Teams object containing all the fetched teams.
     */
    fun getTeams(teamUrlMap: Map<String, String> = getTeamUrlMap()): Teams {
        val teamsUrl = "https://www.eurobasket.com/Slovenia/basketball-Liga-Nova-KBM-Teams.aspx"
        println("Fetching teams from $teamsUrl")
        val teams = Teams()
        skrape(HttpFetcher) {
            request {
                url = teamsUrl
            }
            response {
                htmlDocument {
                    div {
                        withClass = "BasketBallTeamDetails"
                        val entries = findAll("div.BasketBallTeamDetailsLine")
                        entries.forEach { entry ->
                            val logo = entry.findFirst("img").attribute("src")
                            val name = entry.findFirst("div.BasketBallTeamName").text
                            teams.add(
                                BasketballTeam(
                                    name = name,
                                    coach = "/",
                                    director = "/",
                                    logoPath = logo
                                )
                            )
                        }
                    }
                }
            }
        }
        println("Fetched ${teams.size} teams")
        getCoaches(teams, teamUrlMap)
        return teams
    }

    /**
     * Fetches the coaches of the teams from the website.
     *
     * @param teams A Teams object containing the teams. If not provided, it defaults to the teams fetched by getTeams().
     * @param teamUrlMap A map of team names to their corresponding URLs on the website. If not provided, it defaults to the URLs fetched by getTeamMapUrl().
     * @return A Teams object containing all the fetched teams with their coaches.
     */
    fun getCoaches(teams: Teams = getTeams(), teamUrlMap: Map<String, String> = getTeamUrlMap()): Teams {
        val coachPattern = """.*:\s(\w+\s\w+).*""".toRegex()
        teamUrlMap.entries.forEach { (team, link) ->
            skrape(HttpFetcher) {
                request {
                    url = link
                }
                response {
                    htmlDocument {
                        table {
                            withClass = "tblstaff_home"
                            tbody {
                                val row = findFirst("tr#trno1")
                                val coachStr = row.findFirst("td:nth-child(1)").text
                                val coachName = coachPattern.find(coachStr)?.groupValues?.get(1)
                                    ?: "/"
                                teams.find { it.name.contains(team) }?.coach = coachName
                            }
                        }
                    }
                }
            }
        }
        return teams
    }

    /**
     * Fetches the arenas of the teams from the website.
     *
     * @param teams A Teams object containing the teams. If not provided, it defaults to the teams fetched by getTeams().
     * @param teamUrlMap A map of team names to their corresponding URLs on the website. If not provided, it defaults to the URLs fetched by getTeamMapUrl().
     * @return A Stadiums object containing all the fetched arenas.
     */
    fun getArenas(teams: Teams = getTeams(), teamUrlMap: Map<String, String> = getTeamUrlMap()): Stadiums {
        val arenas = Stadiums()
        val arenaPattern = """Home\sCourt:\s(\w+\s\w+)\s\(([\d.,]+)\)""".toRegex()

        teamUrlMap.entries.forEach { (team, link) ->
            println("Fetching arenas from $link")
            skrape(HttpFetcher) {
                request {
                    url = link
                }
                response {
                    htmlDocument {
                        div {
                            withClass = "mobilecolmdnine.col-md-9"
                            val row = findFirst("div.row")
                            val td = try {
                                row.findFirst("td")
                            } catch (e: Exception) {
                                null
                            }

                            if (td != null) {
                                val stadiumImagePath = try {
                                    td.findFirst("a").attribute("href")
                                } catch (e: Exception) {
                                    "/"
                                }
                                try {
                                    val arenaData = arenaPattern.find(td.text)?.groupValues
                                        ?: throw Exception("Arena data not found")
                                    val stadiumName = arenaData[1]
                                    val capacity = arenaData[2].replace(".", "").replace(",", "").toUShort()
                                    val address = row.findFirst("td.tdmobileaddress").text.split('+')[0].trim()
                                    arenas.add(
                                        Stadium(
                                            name = stadiumName,
                                            capacity = capacity,
                                            imagePath = stadiumImagePath,
                                            location = LocationUtil.getLocation(address),
                                            teamId = teams.find { it.name.contains(team) }?.id
                                                ?: throw Exception("Team $team not found")
                                        )
                                    )
                                } catch (e: Exception) {

                                }
                            }
                        }
                    }
                }
            }
        }
        return arenas
    }

    /**
     * Fetches the standings from the website.
     *
     * @param teams A Teams object containing the teams. If not provided, it defaults to the teams fetched by getTeams().
     * @return A Standings object containing all the fetched standings.
     */
    fun getStandings(teams: Teams = getTeams()): Standings {
        val standingsUrl = "https://www.eurobasket.com/Slovenia/basketball-Liga-Nova-KBM-Standings.aspx"
        println("Fetching standings from $standingsUrl")
        val standings = Standings()
        skrape(HttpFetcher) {
            request {
                url = standingsUrl
            }
            response {
                htmlDocument {
                    table {
                        withClass = "FullStandingTable"
                        tbody {
                            val entries = findAll("tr")
                            entries.forEach { entry ->
                                val place = entry.findFirst("td.tdFirstColumn").text.toUShort()
                                val teamName = entry.findFirst("td.tdSecondColumn > a").text.split(' ')[0]
                                val gamesPlayed = entry.findFirst("td:nth-child(3)").text.toUShort()
                                val wins = entry.findFirst("td:nth-child(4)").text.toUShort()
                                val losses = entry.findFirst("td:nth-child(5)").text.toUShort()
                                val goalsData = entry.findFirst("td:nth-child(15)").text.split(' ')[0].split(':')
                                val goalsScored = goalsData[0].toUShort()
                                val goalsConceded = goalsData[1].toUShort()
                                standings.add(
                                    BasketballStanding(
                                        place = place,
                                        team = teams.find { it.name.contains(teamName) }?.id
                                            ?: throw Exception("Team $teamName not found"),
                                        gamesPlayed = gamesPlayed,
                                        wins = wins,
                                        losses = losses,
                                        points = wins,
                                        goalsScored = goalsScored,
                                        goalsConceded = goalsConceded
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        println("Fetched ${standings.size} standings")
        return standings
    }

    /**
     * Fetches the matches from the website.
     *
     * @param teams A Teams object containing the teams. If not provided, it defaults to the teams fetched by getTeams().
     * @param arenas A Stadiums object containing the arenas. If not provided, it defaults to the arenas fetched by getArenas().
     * @return A Matches object containing all the fetched matches.
     */
    fun getMatches(teams: Teams = getTeams(), arenas: Stadiums = getArenas()): Matches {
        val currentYear = LocalDate.now().year
        val dateMap = mapOf(
            "Jan" to currentYear,
            "Feb" to currentYear,
            "Mar" to currentYear,
            "Apr" to currentYear,
            "May" to currentYear,
            "Jun" to currentYear,
            "Jul" to currentYear,
            "Aug" to currentYear,
            "Sep" to currentYear - 1,
            "Oct" to currentYear - 1,
            "Nov" to currentYear - 1,
            "Dec" to currentYear - 1
        )

        val matchesUrl = "https://www.eurobasket.com/Slovenia/basketball-Liga-Nova-KBM.aspx"

        val dateFormatter = SimpleDateFormat("MMM dd yyyy", Locale.Builder().setRegion("SI").build())
        val datePattern = Regex("""(\w{3})[.\s](\d{1,2}):""")
        println("Fetching matches from $matchesUrl")

        val matches = Matches()
        skrape(HttpFetcher) {
            request {
                url = matchesUrl
            }
            response {
                htmlDocument {
                    table {
                        withClass = "GamesScheduleDetailsTable"
                        tbody {
                            val rows = findAll("tr.gamesschedulegames-13-1")
                            rows.forEach { row ->
                                val dateData = datePattern.find(row.findFirst("td:nth-child(1)").text)?.groupValues
                                    ?: throw Exception("Date not found")
                                val dateStr =
                                    "${dateData[1]} ${if (dateData[2].length == 1) "0${dateData[2]}" else dateData[2]} ${dateMap[dateData[1]]}"
                                val date = dateFormatter.parse(dateStr).toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                val home = row.findFirst("td:nth-child(2)").text.split(' ')[0]
                                val homeId = teams.find { it.name.contains(home) }?.id
                                    ?: throw Exception("Team $home not found")
                                val result = row.findFirst("td:nth-child(3)").text.replace('-', ':')
                                val away = row.findFirst("td:nth-child(4)").text.split(' ')[0]
                                val awayId = teams.find { it.name.contains(away) }?.id
                                    ?: throw Exception("Team $away not found")
                                val stadiumId = arenas.find { it.teamId == homeId }?.id
                                matches.add(
                                    Match(
                                        date = date,
                                        home = homeId,
                                        score = result,
                                        away = awayId,
                                        location = "/",
                                        time = "/",
                                        stadium = stadiumId
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        println("Fetched ${matches.size} matches")
        return matches
    }

    /**
     * Fetches all data (teams, coaches, arenas, standings, matches) from the website and saves it in a specified file format.
     *
     * @param fileType The file format in which to save the data. If not provided, it defaults to JSON.
     */
    fun saveAllData(fileType: FileType = FileType.JSON) {
        val teamUrlMap = getTeamUrlMap()
        val teams = getTeams(teamUrlMap = teamUrlMap)
        val arenas = getArenas(teams = teams, teamUrlMap = teamUrlMap)
        val standings = getStandings(teams)
        val matches = getMatches(teams, arenas)
        when (fileType) {
            FileType.CSV -> {
                teams.writeToCSV()
                standings.writeToCSV()
                arenas.writeToCSV()
                matches.writeToCSV()
            }

            FileType.XML -> {
                teams.writeToXML()
                standings.writeToXML()
                arenas.writeToXML()
                matches.writeToXML()
            }

            FileType.JSON -> {
                teams.writeToJSON()
                standings.writeToJSON()
                arenas.writeToJSON()
                matches.writeToJSON()
            }
        }
    }
}