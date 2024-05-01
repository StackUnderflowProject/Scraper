import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.select
import it.skrape.selects.text
import model.BasketballTeam
import model.HandballTeam
import model.Teams
import org.openqa.selenium.By.ByCssSelector
import org.openqa.selenium.By.ByTagName
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import util.ImageUtil
import java.time.Duration

object RZSScraper {
    fun getTeams(): Teams {
        val teams = Teams()

        val searchUrl = "https://www.rokometna-zveza.si/si/tekmovanja/1-a-drl-moski"
        val coachPattern = """.*Trener:\s(([\wšđčćž]+\s*){2})(\s*.*)?""".toRegex(RegexOption.IGNORE_CASE)
        val presidentPattern = """.*Predsednik:\s(([\wšđčćž]+\s*){2})(\s*.*)?""".toRegex(RegexOption.IGNORE_CASE)
        val directorPattern = """.*Direktor:\s(([\wšđčćž]+\s*){2})(\s*.*)?""".toRegex(RegexOption.IGNORE_CASE)
        
        
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

        return teams
    }
    
    fun getTeamLogos(teams: Teams = Teams()) {
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
                ImageUtil.downloadImage(logoUrl, "src/main/resources/handball_team_logos/${teamName}_logo.png")
                team.logoPath = "src/main/resources/handball_team_logos/${teamName}_logo.png"
            }
        }
        driver.quit()
    }
}