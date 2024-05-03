package model

import interfaces.ITeam
import java.io.File

class Teams : ArrayList<ITeam>() {
    fun writeToCSV() {
        val csv = this.joinToString("\n") { it.toCSV() }
        File("teams.csv").writeText(csv)
    }

    fun writeToXML() {
        val xml = "<teams>\n" + this.joinToString("\n") { it.toXML() } + "\n</teams>"
        File("teams.xml").writeText(xml)
    }

    fun writeToJSON() {
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
        File("teams.json").writeText(json)
    }
}