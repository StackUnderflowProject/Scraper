import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.html5.table
import it.skrape.selects.html5.tbody
import model.BasketballTeam
import model.Standings
import model.Teams
import org.openqa.selenium.By.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import util.ImageUtil
import java.time.Duration
import java.util.*

object KZSScraper {

    @Throws(Exception::class)
    fun getTeamIdGeneral(): List<String> {
        val idPattern = Regex("""/ekipa/(\d+).*""")
        val teamsUrl =
            "https://www.kzs.si/tekmovanja/ligaska-tekmovanja/liga-nova-kbm?tab=ekipe&tekmovanje=liga-nova-kbm"

        println("Fetching team IDs from $teamsUrl")
        val driver = ChromeDriver(
            ChromeOptions().apply {
                addArguments("--headless")
            })

        driver.get(teamsUrl)

        val grid = WebDriverWait(driver, Duration.ofSeconds(10)).until(
            ExpectedConditions.presenceOfElementLocated(ByCssSelector(".grid.grid-cols-2.gap-2"))
        )

        val teamIds = mutableListOf<String>()
        grid.findElements(ByTagName("a")).forEach {
            val href = it.getAttribute("href")
            val id = idPattern.find(href)?.groupValues?.get(1) ?: throw Exception("Team ID not found")
            teamIds.add(id)
        }

        driver.quit()
        println("Fetched ${teamIds.size} team IDs")

        return teamIds
    }

    fun getTeamMap(teamIds: List<String>): Map<String, String> {
        println("Fetching team page links from team IDs. This may take a while.")
        val teamMap = mutableMapOf<String, String>()
        teamIds.forEach { id ->
            val queryUrl = "https://www.kzs.si/ekipa/$id?tekmovanje=478&tab=splosno"

            val driver = ChromeDriver(
                ChromeOptions().apply {
                    addArguments("--headless")
                })

            driver.get(queryUrl)

            val anchor = WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(ByCssSelector("a.cursor-pointer"))
            )


            val teamName = anchor.text.trim(' ')
            val link = anchor.getAttribute("href")
            teamMap[teamName] = link
            driver.quit()

        }
        println("Fetched ${teamMap.size} team page links")
        return teamMap
    }


    fun getTeams(teamMap: Map<String, String>): Teams {
        val urlPattern = """.*url\((.*)\).*\n.*""".toRegex()
        val teams = Teams()
        
        println("Fetching team data from team pages. This may take a while.")
        teamMap.forEach { (teamName, link) ->
            val driver = ChromeDriver(
                ChromeOptions().apply {
                    addArguments("--headless")
                })
            driver.get(link)

            val logoContainer = WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(ByCssSelector("div.mb-8"))
            )

            val logoUrl = urlPattern.find(logoContainer.getAttribute("innerHTML"))
                ?.groupValues?.get(1) ?: throw Exception("Logo URL not found")
            
            

            val data = WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.presenceOfElementLocated(ByCssSelector("div.content-container"))
            ).text.split("\n")
            driver.quit()

            val directorIdx = data.indexOfFirst { it.contains("Direktor:") }
            val director = if (directorIdx >= 0) data[directorIdx + 1] else "/"

            val coachIdx = data.indexOfFirst { it.contains("Glavni trener:") }
            val coach = if (coachIdx >= 0) data[coachIdx + 1] else "/"

            val logoPath = "src/main/resources/basketball_team_logos/${teamName}_logo.png"
            ImageUtil.downloadImage(logoUrl, logoPath)
            
            teams.add(
                BasketballTeam(
                    name = teamName,
                    director = director,
                    coach = coach,
                    logoPath = logoPath
                )
            )
        }
        println("Fetched ${teams.size} teams")
        return teams
    }

    
    fun getStandings(): Standings {
        // TODO: Implement this method
        val standings = Standings()

        val standingsUrl =
            "https://www.google.com/search?q=liga+nova+kbm+lestvica&oq=liga+nova+kbm+lestvica&gs_lcrp=EgZjaHJvbWUyBggAEEUYOTIGCAEQLhhA0gEJMTAzMzJqMGoxqAIIsAIB&sourceid=chrome&ie=UTF-8#sie=t;/m/02wbgdz;3;;st;fp;1;;;"

        skrape(HttpFetcher) {
            request {
                url = standingsUrl
            }
            response { 
                htmlDocument { 
                    println(this)
                }
            }
        }
        
        
        return standings
    }

}