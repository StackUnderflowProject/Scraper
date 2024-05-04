package model

import interfaces.ITeam
import java.io.File

class Teams : ArrayList<ITeam>() {
    fun writeToCSV(path: String = "teams.csv") {
        val csv = this.joinToString("\n") { it.toCSV() }
        File(path).writeText(csv)
    }

    fun writeToXML(path: String = "teams.xml") {
        val xml = "<teams>\n" + this.joinToString("\n") { it.toXML() } + "\n</teams>"
        File(path).writeText(xml)
    }

    fun writeToJSON(path: String = "teams.json") {
        val json = """
{
    "teams": [
        ${
            this.joinToString("\n") {
                var team = it.toJSON()
                if (this.indexOf(it) == this.size - 1) {
                    team = team.removeSuffix(",")
                }
                team
            }
        }
    ]
}""".trimIndent()
        File(path).writeText(json)
    }
}