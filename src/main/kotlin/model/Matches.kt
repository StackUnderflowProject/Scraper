package model

import java.io.File

class Matches : ArrayList<Match>() {
    fun writeToCSV(path: String = "matches.csv") {
        val csv = this.joinToString("\n") { it.toCSV() }
        File(path).writeText(csv)
    }

    fun writeToXML(path: String = "matches.xml") {
        val xml = "<matches>\n" + this.joinToString("\n") { it.toXML() } + "\n</matches>"
        File(path).writeText(xml)
    }

    fun writeToJSON(path: String = "matches.json") {
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
        File(path).writeText(json)
    }

}