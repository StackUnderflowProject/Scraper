package scrapers

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import model.*
import org.openqa.selenium.By.ByCssSelector
import org.openqa.selenium.By.ByTagName
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import util.ImageUtil
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RZSScraper {
    fun getTeams(downloadImage: Boolean = false): Teams {
        val teams = Teams()

        val searchUrl = "https://www.rokometna-zveza.si/si/tekmovanja/1-a-drl-moski"
        val coachPattern = """.*Trener:\s(([\wšđčćž]+\s*){2})(\s*.*)?""".toRegex(RegexOption.IGNORE_CASE)
        val presidentPattern = """.*Predsednik:\s(([\wšđčćž]+\s*){2})(\s*.*)?""".toRegex(RegexOption.IGNORE_CASE)
        val directorPattern = """.*Direktor:\s(([\wšđčćž]+\s*){2})(\s*.*)?""".toRegex(RegexOption.IGNORE_CASE)

        println("Fetching teams...")
        skrape(HttpFetcher) {
            request {
                url = searchUrl
            }
            response {
                htmlDocument {
                    val teamNames = findAll("div.contentTitle h3").map { it.text }

                    val teamDataContainers = findAll("div.paragraph.paragraph-normal")
                    teamDataContainers.forEachIndexed { idx, container ->
                        val coach = coachPattern.find(container.text)?.groups?.get(1)?.value ?: "/"
                        val president = presidentPattern.find(container.text)?.groups?.get(1)?.value ?: "/"
                        val director = directorPattern.find(container.text)?.groups?.get(1)?.value ?: "/"
                        teams.add(
                            HandballTeam(
                                name = teamNames[idx],
                                coach = coach,
                                president = president,
                                director = director
                            )
                        )
                    }
                }
            }
        }

        getTeamLogos(teams, downloadImage)
        println("Teams fetched successfully!")
        return teams
    }

    private fun getTeamLogos(teams: Teams, downloadImage: Boolean = false) {
        println("Fetching team logos...")
        val teamsUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/70/ekipe"
        val driver = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        driver.get(teamsUrl)

        val table = WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(ByCssSelector("tbody"))
        )

        val rows = table.findElements(ByCssSelector("tr"))
        rows.forEach { row ->
            val teamName = row.findElement(ByTagName("h6")).text
            val logoUrl = row.findElement(ByCssSelector("img")).getAttribute("src")
            val team = teams.find { it.name == teamName }
            if (team != null) {
                if(downloadImage) {
                    ImageUtil.downloadImage(logoUrl, "src/main/resources/handball_team_logos/${teamName}_logo.png")
                    team.logoPath = "src/main/resources/handball_team_logos/${teamName}_logo.png"
                } else {
                    team.logoPath = logoUrl
                }
            }
        }
        driver.quit()
        println("Team logos fetched successfully!")
    }

    fun getMatches(team: String = "", teams: Teams = getTeams(), arenas: Stadiums = getArenas()) : Matches {
        val matchesUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/70/razpored"
        val datePattern = """.*-\s(\d{2}\.\d{2}\.\d{4}\s\d{2}:\d{2}).*""".toRegex(RegexOption.IGNORE_CASE)
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val matches = Matches()

        println("Fetching matches...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        chrome.get(matchesUrl)

        val matchLegContainers = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(ByCssSelector("aside.widget.widget--sidebar.card.card--has-table.widget-leaders"))
        )

        matchLegContainers.forEach { matchLegContainer ->
            val matchList = matchLegContainer.findElements(ByCssSelector("li.widget-results__item"))
            matchLoop@ for (match in matchList) {
                try {
                    val home =
                        match.findElement(ByCssSelector("div.widget-results__team--first > div > h5.widget-results__team-name")).text

                    val away =
                        match.findElement(ByCssSelector("div.widget-results__team--second > div > h5.widget-results__team-name")).text

                    if(team.isNotEmpty() && (home != team && away != team)) {
                        break@matchLoop
                    }

                    val dateStr =
                        datePattern.find(match.findElement(ByCssSelector("div.widget-results__title strong")).text)
                            ?.groups?.get(1)?.value ?: throw Exception("Date not found")

                    val date = LocalDateTime.from(dateFormatter.parse(dateStr))

                    val arena = match.findElement(ByCssSelector("div.widget-results__title > strong:nth-child(2)")).text

                    val score = match.findElement(ByCssSelector("div.widget-results__score div")).text

                    val played = score != "0 - 0"

                    val time = String.format("%02d:%02d", date.hour, date.minute)
                    matches.add(
                        Match(
                            date = date.toLocalDate(),
                            stadium = arenas.find { it.name.lowercase() == arena.lowercase() }?.id,
                            home = teams.find { it.name == home }?.id ?: throw Exception("Team not found"),
                            away = teams.find { it.name == away }?.id ?: throw Exception("Team not found"),
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

        chrome.quit()
        println("Matches fetched successfully!")
        return matches
    }
    
    fun getStandings(teams: Teams = getTeams()): Standings {
        val standings = Standings()
        val standingsUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/70/lestvica"

        println("Fetching standings...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })
        
        chrome.get(standingsUrl)
        
        val table = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(ByCssSelector("tbody"))
        )
        
        val rows = table.findElements(ByCssSelector("tr"))
        rows.forEach {row ->
            val place = row.findElement(ByCssSelector("td.game-player-result__date > h6")).text.toInt()
            val team = row.findElement(ByCssSelector("td.game-player-result__vs > a > div > div > h6.team-meta__name")).text
            val gamesPlayed = row.findElement(ByCssSelector("td.game-player-result__score")).text.toInt()
            val wins = row.findElement(ByCssSelector("td.game-player-result__min")).text.toInt()
            val draws = row.findElement(ByCssSelector("td.game-player-result__ts")).text.toInt()
            val losses = row.findElement(ByCssSelector("td.game-player-result__tg")).text.toInt()
            val goalData = row.findElement(ByCssSelector("td.game-player-result__st")).text.split(":")
            val goalsScored = goalData[0].toInt()
            val goalsConceded = goalData[1].toInt()
            val points = row.findElement(ByCssSelector("td.game-player-result__ga > span.team-info__value")).text.toInt()
            
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
        
        chrome.quit()
        println("Standings fetched successfully!")
        return standings
    }
    
    fun getArenas(teams: Teams = getTeams()): Stadiums {
        val arenas = Stadiums()
        val arenasUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/70/ekipe"
        
        println("Fetching arenas...")
        val chrome = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })
        
        chrome.get(arenasUrl)
        
        val table = WebDriverWait(chrome, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(ByCssSelector("tbody"))
        )
        
        val rows = table.findElements(ByCssSelector("tr"))
        rows.forEach { row ->
            val teamName = row.findElement(ByTagName("h6")).text
            val arenaArr = row.findElement(ByCssSelector("td:nth-child(4)")).text.split('\n')
            arenaArr.forEach { arena ->
                val team = teams.find { it.name == teamName }
                if(team != null) {
                    arenas.add(
                        Stadium(
                            name = arena,
                            teamId = team.id
                        )
                    )
                }
                
            }
        }
        
        chrome.quit()
        println("Arenas fetched successfully!")
        return arenas
    }
    
    fun saveAllData(fileType: FileType = FileType.JSON, downloadImage: Boolean = false) {
        val teams = getTeams(downloadImage)
        val arenas = getArenas(teams = teams)
        val standings = getStandings(teams = teams)
        val matches = getMatches(teams = teams, arenas = arenas)
        
        when(fileType) {
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