package model

import java.io.File

class Stadiums : ArrayList<Stadium>() {
    fun writeToCSV(path: String = "stadiums.csv") {
        val csv = this.joinToString("\n") { it.toCSV() }
        File(path).writeText(csv)
    }

    fun writeToXML(path: String = "stadiums.xml") {
        val xml = """
            <stadiums>
                ${this.joinToString("\n") { it.toXML() }}
            </stadiums>
        """.trimIndent()
        File(path).writeText(xml)
    }

    fun writeToJSON(path: String = "stadiums.json"){
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
        File(path).writeText(json)
    }
}