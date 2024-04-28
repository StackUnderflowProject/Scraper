fun main() {
    val teamIds = KZSScraper.getTeamIdGeneral()
    val teamMap = KZSScraper.getTeamMap(teamIds = teamIds)
    val teams = KZSScraper.getTeams(teamMap)

    teams.writeToJSON()
    teams.writeToXML()
    teams.writeToCSV()
}
