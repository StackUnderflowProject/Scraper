package util

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

object LocationUtil {
    data class Location(val lat: Double, val lng: Double)
    data class Geometry(val location: Location)
    data class Result(val geometry: Geometry)
    data class GeocodeResponse(val results: List<Result>)

    // TODO set own map api key
    private const val MAP_API_KEY = "AIzaSyDVd_pBgyIrTNWl9iizuJcMiwSVcTk4lR8"

    /**
     * Fetches the geographical location (latitude and longitude) of a given address.
     *
     * @param address The address to geocode.
     * @return A Location object containing the latitude and longitude of the address, or null if the address could not be geocoded.
     */
    fun getLocation(address: String): Location? {
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        val apiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedAddress&key=$MAP_API_KEY"

        try {
            val url = URI.create(apiUrl).toURL()
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // Parse JSON response
                val gson = Gson()
                val jsonResponse = response.toString()
                val geocodeResponse = gson.fromJson(jsonResponse, GeocodeResponse::class.java)

                if (geocodeResponse.results.isNotEmpty()) {
                    val location = geocodeResponse.results[0].geometry.location
                    return location
                }
            }

        } catch (e: Exception) {
            println("Error: $e")
        }

        return null
    }
}