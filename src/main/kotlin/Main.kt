import scrapers.RZSScraper

fun main() {
    val teams = RZSScraper.getTeams()
    val arenas = RZSScraper.getArenas(teams)
    val matches = RZSScraper.getMatches(teams = teams, arenas = arenas)
    matches.writeToJSON()
    arenas.writeToJSON()
    teams.writeToJSON()
}
