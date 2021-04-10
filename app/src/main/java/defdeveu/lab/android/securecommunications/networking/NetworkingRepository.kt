package defdeveu.lab.android.securecommunications.networking

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkingRepository {
    suspend fun getResponseFromUrl(
            url: String, xml: String
    ): NetworkingContract.Result<String> {
        return withContext(Dispatchers.IO) {
            makeUrlCall(url, xml)
        }
    }

    private fun makeUrlCall(url: String, xml: String): NetworkingContract.Result<String> {
        val urlEncoded = URL(url)
        (urlEncoded.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/xml")
            doInput = true
            doOutput = true

            val writer = BufferedWriter(
                    OutputStreamWriter(outputStream, "UTF-8"))
            writer.write(xml)
            writer.flush()
            writer.close()

            val reader = BufferedReader(InputStreamReader(inputStream))
            val total = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                total.append(line)
            }

            Log.d(javaClass.name, "Received: $total")
            return NetworkingContract.Result.Success(total.toString())
        }
        return NetworkingContract.Result.Error(Exception("Cannot open HttpURLConnection"))
    }
}