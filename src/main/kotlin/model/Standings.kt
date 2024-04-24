package model

import java.io.File

class Standings : ArrayList<FootballStanding>() {
    fun writeToCSV() {
        val csv = this.joinToString("\n") { it.toCSV() }
        File("standings.csv").writeText(csv)
    }

    fun writeToXML() {
        val xml = "<standings>\n" + this.joinToString("\n") { it.toXML() } + "\n</standings>"
        File("standings.xml").writeText(xml)
    }

    fun writeToJSON() {
        val json = """
{
    "standings": [
        ${
            this.joinToString("\n") {
                var standing = it.toJSON()
                if (this.indexOf(it) == this.size - 1) {
                    standing = standing.removeSuffix(",")
                }
                standing
            }
        }
    ]
}""".trimIndent()
        File("standings.json").writeText(json)
    }
}