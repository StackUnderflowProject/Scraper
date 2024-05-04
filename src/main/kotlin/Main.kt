import scrapers.KZSScraper
import scrapers.PLTScraper
import scrapers.RZSScraper

fun main() {
    val teams = KZSScraper.getTeams()
    KZSScraper.getStandings(teams).writeToJSON()
    teams.writeToJSON()
}
