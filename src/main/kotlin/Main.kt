import scrapers.RZSScraper

fun main() {
    val standings = RZSScraper.getStandings()
    standings.forEach(::println)
}
