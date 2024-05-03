package model

import interfaces.IStanding
import java.io.File

class Standings : ArrayList<IStanding>() {
    fun writeToCSV(path: String = "standings.csv") {
        val csv = this.joinToString("\n") { it.toCSV() }
        File(path).writeText(csv)
    }

    fun writeToXML(path: String = "standings.xml") {
        val xml = "<standings>\n" + this.joinToString("\n") { it.toXML() } + "\n</standings>"
        File(path).writeText(xml)
    }

    fun writeToJSON(path: String = "standings.json") {
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
        File(path).writeText(json)
    }
}