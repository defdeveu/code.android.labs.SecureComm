package defdeveu.lab.android.securecommunications.networking

import android.util.Base64

object NetworkingContract {
    fun createRequestXML(rsaSignature: ByteArray?, rsaResult: String?, aesResult: String?): String {
        val rsaSignatureString = Base64.encodeToString(rsaSignature, Base64.DEFAULT)
        return "<?xml version=\"1.0\" ?>" +
                "<request>" +
                "<enckey>" + rsaResult + "</enckey>" +
                "<message>" + aesResult + "</message>" +
                "<signature>" + rsaSignatureString + "</signature>" +
                "</request>"
    }

    sealed class Result<out R> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }
}