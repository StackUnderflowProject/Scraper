fun main(){
    val stadiums = PLTScraper.getStadiums(PLTScraper.getTeamMap(2024))
    stadiums.writeToJSON()
    stadiums.writeToXML()
    stadiums.writeToCSV()
}