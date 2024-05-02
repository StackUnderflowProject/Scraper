fun main() {
    val standings = RZSScraper.getStandings()
    standings.forEach(::println)
}
