package defdeveu.lab.android.securecommunications

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import defdeveu.lab.android.securecommunications.networking.Crypto
import defdeveu.lab.android.securecommunications.networking.Crypto.decryptAES
import defdeveu.lab.android.securecommunications.networking.Crypto.encryptRSA
import defdeveu.lab.android.securecommunications.networking.Crypto.signRSA
import defdeveu.lab.android.securecommunications.networking.NetworkingContract
import defdeveu.lab.android.securecommunications.networking.NetworkingRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val networkingRepository = NetworkingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        button_send.setOnClickListener {
            progress.visibility = View.VISIBLE
            startCommunication(edit_text_message.text.toString())
            edit_text_message.text.clear()
        }
    }

    private fun startCommunication(msg: String) {
        val plain = msg.toByteArray()

        try {
            val aesResult = Crypto.encryptAES(key, plain, iv)
            val aesResultString = Base64.encodeToString(aesResult, Base64.DEFAULT)
            Log.d(javaClass.name, "AES encryption result: $aesResultString")

            val toEncrypt = "$keyStr|$ivStr"
            val rsaResult = toEncrypt.toByteArray().encryptRSA(resources)
            val rsaResultString = Base64.encodeToString(rsaResult, Base64.DEFAULT)
            Log.d(javaClass.name, "RSA encryption result: $rsaResultString")

            val rsaSignature = aesResult.signRSA(resources)
            Log.d(javaClass.name, "RSA signature: " + rsaSignature.toString())

            CoroutineScope(Dispatchers.Default).launch {
                val response = networkingRepository.getResponseFromUrl(
                        ENDPOINT_URL,
                        NetworkingContract.createRequestXML(rsaSignature, rsaResultString, aesResultString)
                )
                CoroutineScope(Dispatchers.Main).launch {
                    when (response) {
                        is NetworkingContract.Result.Success -> displaySuccessResult(response.data)
                        is NetworkingContract.Result.Error -> displayErrorResult(response.exception)
                    }
                }
            }
        } catch (e: Exception) {
            edit_text_response.text = e.localizedMessage
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displaySuccessResult(resultText: String){
        progress.visibility = View.GONE
        val endIndex = resultText.indexOf("</response>")
        val subString = resultText.substring(10, endIndex)
        val encResp = Base64.decode(subString, Base64.DEFAULT)
        try {
            decryptAES(key, encResp, iv)?.let {
                edit_text_response.text = Base64.decode(it, Base64.DEFAULT).toString(Charset.defaultCharset())
            }
        } catch (e: Exception) {
            edit_text_response.text = "Error decrypting $subString"
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayErrorResult(exception: Exception){
        progress.visibility = View.GONE
        edit_text_response.text = "Error: ${exception.localizedMessage}"
    }

    companion object {
        const val ENDPOINT_URL = "https://zs.labs.defdev.eu:9998/request"
        const val keyStr = "00112233445566778899aabbccddeeff"
        const val ivStr = "1111111111111111"
        val key = keyStr.toByteArray()
        val iv = ivStr.toByteArray()
    }
}