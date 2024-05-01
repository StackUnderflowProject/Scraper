fun main() {
    val matches = RZSScraper.getMatches("MRK LJUBLJANA")
    matches.forEach(::println)
}
