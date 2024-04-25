package model

import java.io.File

class Stadiums : ArrayList<Stadium>() {
    fun writeToCSV() {
        val csv = this.joinToString("\n") { it.toCSV() }
        File("stadiums.csv").writeText(csv)
    }

    fun writeToXML() {
        val xml = """
            <stadiums>
                ${this.joinToString("\n") { it.toXML() }}
            </stadiums>
        """.trimIndent()
        File("stadiums.xml").writeText(xml)
    }

    fun writeToJSON(){
        val json = """
{
    "stadiums": [
        ${
            this.joinToString("\n") {
                var stadium = it.toJSON()
                if (this.indexOf(it) == this.size - 1) {
                    stadium = stadium.removeSuffix(",")
                }
                stadium
            }
        }
    ]
}
        """.trimIndent()
        File("stadiums.json").writeText(json)
    }
}