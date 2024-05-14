package scrapers

import model.*
import org.openqa.selenium.By.ByCssSelector
import org.openqa.selenium.By.ByTagName
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import util.LocationUtil
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RZSScraper {
    private val seasonMap = mapOf(
        2024 to 70,
        2023 to 69,
        2022 to 66,
        2021 to 65,
        2020 to 64,
        2019 to 63,
        2018 to 59,
    )

    /**
     * Fetches team data from the RZS website for a given season.
     *
     * @param season The season for which to fetch the team data. Defaults to the current year.
     * @return A Teams object containing all the fetched teams.
     */
    fun getTeams(season: Int = LocalDate.now().year): Teams {
        val teams = Teams()
        val searchUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/${seasonMap[season]}/ekipe"

        println("Fetching teams...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        try {
            chrome.get(searchUrl)

            val table = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(ByCssSelector("tbody"))
            )

            val rows = table.findElements(ByCssSelector("tr"))
            rows.forEach { row ->
                val teamLogo = row.findElement(ByCssSelector("img")).getAttribute("src")
                val teamName = row.findElement(ByTagName("h6")).text
                val siteLink = row.findElement(ByCssSelector("td:nth-child(5) > a")).getAttribute("href")
                val coach = getCoach(siteLink)
                teams.add(
                    HandballTeam(
                        name = teamName,
                        coach = coach,
                        president = "/",
                        director = "/",
                        logoPath = teamLogo
                    )
                )
            }
        } finally {
            chrome.quit()
        }
        println("Teams fetched successfully!")
        return teams
    }

    /**
     * Fetches the coach's name for a given team from the RZS website.
     *
     * @param teamSite The URL of the team's page on the RZS website.
     * @return A String containing the coach's name.
     */
    private fun getCoach(teamSite: String): String {
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        try {
            chrome.get(teamSite)

            val coachElement = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(
                    ByCssSelector(
                        "div.row div.col-md-6 aside.widget-team-info:not([data-v-386fec58]) div.widget__content ul.team-info-list li:nth-child(7) span.team-info__value"
                    )
                )
            )
            val coach = coachElement.text
            return coach
        } finally {
            chrome.quit()
        }
    }

    /**
     * Fetches match data from the RZS website for a given season.
     *
     * @param season The season for which to fetch the match data. Defaults to the current year.
     * @param teams The Teams object containing all the teams for the season. Defaults to the result of the getTeams() function.
     * @param arenas The Stadiums object containing all the arenas for the season. Defaults to the result of the getArenas() function.
     * @return A Matches object containing all the fetched matches.
     */
    fun getMatches(
        season: Int = LocalDate.now().year,
        teams: Teams = getTeams(),
        arenas: Stadiums = getArenas()
    ): Matches {
        val matchesUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/${seasonMap[season]}/razpored"
        val datePattern = """.*-\s(\d{2}\.\d{2}\.\d{4}\s\d{2}:\d{2}).*""".toRegex(RegexOption.IGNORE_CASE)
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val matches = Matches()

        println("Fetching matches...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        try {
            chrome.get(matchesUrl)

            val matchLegContainers = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(ByCssSelector("aside.widget.widget--sidebar.card.card--has-table.widget-leaders"))
            )

            matchLegContainers.forEach { matchLegContainer ->
                val matchList = matchLegContainer.findElements(ByCssSelector("li.widget-results__item"))
                matchList.forEach { match ->
                    try {
                        val home =
                            match.findElement(ByCssSelector("div.widget-results__team--first")).text

                        val away =
                            match.findElement(ByCssSelector("div.widget-results__team--second")).text

                        val dateStr =
                            datePattern.find(match.findElement(ByCssSelector("div.widget-results__title strong")).text)
                                ?.groups?.get(1)?.value ?: throw Exception("Date not found")

                        val date = LocalDateTime.from(dateFormatter.parse(dateStr))

                        val arena =
                            match.findElement(ByCssSelector("div.widget-results__title > strong:nth-child(2)")).text

                        val score = match.findElement(ByCssSelector("div.widget-results__score div")).text

                        val played = score != "0 - 0"

                        val time = String.format("%02d:%02d", date.hour, date.minute)

                        val stadiumId = arenas.find { it.name.lowercase() == arena.lowercase() }?.id
                        val homeTeam = teams.find { it.name == home }?.id
                        val awayTeam = teams.find { it.name == away }?.id
                        matches.add(
                            Match(
                                date = date.toLocalDate(),
                                stadium = stadiumId,
                                home = homeTeam ?: throw Exception("Home team not found"),
                                away = awayTeam ?: throw Exception("Away team not found"),
                                score = score,
                                location = arena,
                                time = time,
                                played = played
                            )
                        )

                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
        } finally {
            chrome.quit()
        }
        println("Matches fetched successfully!")
        return matches
    }

    /**
     * Fetches the standings from the RZS website for a given season.
     *
     * @param season The season for which to fetch the standings. Defaults to the current year.
     * @param teams The Teams object containing all the teams for the season. Defaults to the result of the getTeams() function.
     * @return A Standings object containing all the fetched standings.
     */
    fun getStandings(season: Int = LocalDate.now().year, teams: Teams = getTeams()): Standings {
        val standings = Standings()
        val standingsUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/${seasonMap[season]}/lestvica"

        println("Fetching standings...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })
        try {
            chrome.get(standingsUrl)

            val table = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(ByCssSelector("tbody"))
            )

            val rows = table.findElements(ByCssSelector("tr"))
            rows.forEach { row ->
                val place = row.findElement(ByCssSelector("td.game-player-result__date > h6")).text.toInt()
                val team =
                    row.findElement(ByCssSelector("td.game-player-result__vs > a > div > div > h6.team-meta__name")).text
                val gamesPlayed = row.findElement(ByCssSelector("td.game-player-result__score")).text.toInt()
                val wins = row.findElement(ByCssSelector("td.game-player-result__min")).text.toInt()
                val draws = row.findElement(ByCssSelector("td.game-player-result__ts")).text.toInt()
                val losses = row.findElement(ByCssSelector("td.game-player-result__tg")).text.toInt()
                val goalData = row.findElement(ByCssSelector("td.game-player-result__st")).text.split(":")
                val goalsScored = goalData[0].toInt()
                val goalsConceded = goalData[1].toInt()
                val points =
                    row.findElement(ByCssSelector("td.game-player-result__ga > span.team-info__value")).text.toInt()

                try {
                    standings.add(
                        DrawableStanding(
                            place = place.toUShort(),
                            team = teams.find { it.name == team }?.id ?: throw Exception("Team not found"),
                            gamesPlayed = gamesPlayed.toUShort(),
                            wins = wins.toUShort(),
                            draws = draws.toUShort(),
                            losses = losses.toUShort(),
                            goalsScored = goalsScored.toUShort(),
                            goalsConceded = goalsConceded.toUShort(),
                            points = points.toUShort()
                        )
                    )
                } catch (e: Exception) {
                    println(e)
                }
            }
        } finally {
            chrome.quit()
        }
        println("Standings fetched successfully!")
        return standings
    }

    /**
     * Fetches the arenas from the RZS website for a given season.
     *
     * @param season The season for which to fetch the arenas. Defaults to the current year.
     * @param teams The Teams object containing all the teams for the season. Defaults to the result of the getTeams() function.
     * @return A Stadiums object containing all the fetched arenas.
     */
    fun getArenas(season: Int = LocalDate.now().year, teams: Teams = getTeams()): Stadiums {
        val arenas = Stadiums()
        val arenasUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/${seasonMap[season]}/ekipe"
        println("Fetching arenas...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        try {
            chrome.get(arenasUrl)

            val table = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(ByCssSelector("tbody"))
            )

            val rows = table.findElements(ByCssSelector("tr"))
            rows.forEach { row ->
                val teamName = row.findElement(ByTagName("h6")).text
                val address = row.findElement(ByCssSelector("td:nth-child(2)")).text.replace("\n", " ").trim(',').trim()
                val arena = row.findElement(ByCssSelector("td:nth-child(4)")).text.split('\n')[0]
                val team = teams.find { it.name == teamName }
                if (team != null) {
                    arenas.add(
                        Stadium(
                            name = arena,
                            teamId = team.id,
                            location = LocationUtil.getLocation(address),
                        )
                    )
                }
            }
        } finally {
            chrome.quit()
        }
        println("Arenas fetched successfully!")
        return arenas
    }

    /**
     * Fetches all data (teams, arenas, standings, matches) for a given season and saves it in a specified file format.
     *
     * @param season The season for which to fetch the data. Defaults to the current year.
     * @param fileType The file format in which to save the data. Defaults to JSON.
     *
     * The function performs the following steps:
     * 1. Fetches the teams for the given season.
     * 2. Fetches the arenas for the given season.
     * 3. Fetches the standings for the given season.
     * 4. Fetches the matches for the given season.
     * 5. Depending on the specified file format, it saves the fetched data in JSON, XML, or CSV files.
     */
    fun saveAllData(season: Int = LocalDate.now().year, fileType: FileType = FileType.JSON) {
        val teams = getTeams(season)
        val arenas = getArenas(season = season, teams = teams)
        val standings = getStandings(season = season, teams = teams)
        val matches = getMatches(season = season, teams = teams, arenas = arenas)

        when (fileType) {
            FileType.JSON -> {
                teams.writeToJSON("teams.json")
                standings.writeToJSON("standings.json")
                arenas.writeToJSON("arenas.json")
                matches.writeToJSON("matches.json")
            }

            FileType.XML -> {
                teams.writeToXML("teams.xml")
                standings.writeToXML("standings.xml")
                arenas.writeToXML("arenas.xml")
                matches.writeToXML("matches.xml")
            }

            FileType.CSV -> {
                teams.writeToCSV("teams.csv")
                standings.writeToCSV("standings.csv")
                arenas.writeToCSV("arenas.csv")
                matches.writeToCSV("matches.csv")
            }
        }
    }
}