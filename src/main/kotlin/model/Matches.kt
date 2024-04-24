package model

import java.io.File

class Matches : ArrayList<Match>() {
    fun writeToCSV() {
        val csv = this.joinToString("\n") { it.toCSV() }
        File("matches.csv").writeText(csv)
    }

    fun writeToXML() {
        val xml = "<matches>\n" + this.joinToString("\n") { it.toXML() } + "\n</matches>"
        File("matches.xml").writeText(xml)
    }

    fun writeToJSON() {
        val json = """
{
    "matches": [
        ${
            this.joinToString("\n") {
                var match = it.toJSON()
                if (this.indexOf(it) == this.size - 1) {
                    match = match.removeSuffix(",")
                }
                match
            }
        }
    ]
}""".trimIndent()
        File("matches.json").writeText(json)
    }

}