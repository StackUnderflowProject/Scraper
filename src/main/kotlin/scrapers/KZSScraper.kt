package scrapers

import com.sun.jdi.event.LocatableEvent
import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.html5.div
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import model.*
import net.bytebuddy.asm.Advice.Local
import org.openqa.selenium.By.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import util.ImageUtil
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

object KZSScraper {
    fun getTeams(): Teams {
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
        return teams
    }

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

                                println("Place: $place, Team: $teamName, Games Played: $gamesPlayed, Wins: $wins, Losses: $losses, Goals Scored: $goalsScored, Goals Conceded: $goalsConceded")
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

    fun getMatches(teams: Teams = getTeams()): Matches {
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
                                val dateStr = "${dateData[1]} ${if(dateData[2].length ==1) "0${dateData[2]}" else dateData[2]} ${dateMap[dateData[1]]}"
                                val date = dateFormatter.parse(dateStr).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                val home = row.findFirst("td:nth-child(2)").text.split(' ')[0]
                                val result = row.findFirst("td:nth-child(3)").text.replace('-', ':')
                                val away = row.findFirst("td:nth-child(4)").text.split(' ')[0]
                                matches.add(
                                    Match(
                                        date = date,
                                        home = teams.find { it.name.contains(home) }?.id
                                            ?: throw Exception("Team $home not found"),
                                        score = result,
                                        away = teams.find { it.name.contains(away) }?.id
                                            ?: throw Exception("Team $away not found"),
                                        location = "/",
                                        time = "/"
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
    
    fun saveAllData(fileType: FileType = FileType.JSON){
        val teams = getTeams()
        val standings = getStandings(teams)
        val matches = getMatches(teams)
        when(fileType){
            FileType.CSV -> {
                teams.writeToCSV()
                standings.writeToCSV()
                matches.writeToCSV()
            }
            FileType.XML -> {
                teams.writeToXML()
                standings.writeToXML()
                matches.writeToXML()
            }
            FileType.JSON -> {
                teams.writeToJSON()
                standings.writeToJSON()
                matches.writeToJSON()
            }
        }
    }
}