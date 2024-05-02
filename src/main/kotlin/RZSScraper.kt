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

        getTeamLogos(teams)
        return teams
    }

    private fun getTeamLogos(teams: Teams) {
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

    fun getMatches(team: String = ""): Matches {
        val matchesUrl = "https://livestat.rokometna-zveza.si/#/liga/1155/sezona/70/razpored"
        val datePattern = """.*-\s(\d{2}\.\d{2}\.\d{4}\s\d{2}:\d{2}).*""".toRegex(RegexOption.IGNORE_CASE)
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val matches = Matches()

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
                            stadium = arena,
                            home = home,
                            away = away,
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
        return matches
    }
    
    
    
}