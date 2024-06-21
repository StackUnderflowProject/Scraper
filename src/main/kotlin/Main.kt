import scrapers.PLTScraper
import scrapers.RZSScraper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate

fun getScraper(): Int {
    var scraper = -1
    while (scraper > 2 || scraper < 0) {
        println(
            """
Please enter the number of the scraper you would like to use:
    1. PLTScraper
    2. RZSScraper
    0. Exit
            """.trimMargin()
        )
        print("Scraper number: ")

        // Use BufferedReader for input handling
        val reader = BufferedReader(InputStreamReader(System.`in`))
        try {
            scraper = reader.readLine()?.toInt() ?: -1
        } catch (e: NumberFormatException) {
            println("Invalid input. Please enter a valid number.")
        }
    }
    return scraper
}

fun getSeason(): Int {
    var season = 0
    while (season < 2020 || season > LocalDate.now().year) {
        println(
            """
            Please enter the season you would like to scrape (2020-${LocalDate.now().year}). The default is the current season.
            """.trimIndent()
        )
        print("Season: ")

        // Use BufferedReader for input handling
        val reader = BufferedReader(InputStreamReader(System.`in`))
        try {
            season = reader.readLine()?.toInt() ?: LocalDate.now().year
        } catch (e: NumberFormatException) {
            println("Invalid input. Please enter a valid number.")
        }
    }
    return season
}

fun main() {
    println(
        """
            Welcome to the Scraper! Please select the scraper you would like to use.
            Then the season you would like to scrape. The default is the current season.
            The scraper will save the data to separate json files.
        """.trimIndent()
    )
    val scraper = getScraper()
    if(scraper == 0) {
        println("Exiting...")
        return
    }
    val season = getSeason().toUShort()
    when (scraper) {
        1 -> PLTScraper.saveAllData(season)
        2 -> RZSScraper.saveAllData(season)
        else -> println("Invalid scraper number.")
    }
}