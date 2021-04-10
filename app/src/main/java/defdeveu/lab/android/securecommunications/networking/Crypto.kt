package defdeveu.lab.android.securecommunications.networking

import android.content.res.Resources
import android.util.Base64
import defdeveu.lab.android.securecommunications.R
import java.io.IOException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Crypto {
    @Throws(IOException::class, InvalidKeySpecException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class)
    fun ByteArray.encryptRSA(resources: Resources): ByteArray? {
        val instream = resources.openRawResource(R.raw.server_public_pkcs8)
        val encodedKey = ByteArray(instream.available())
        instream.read(encodedKey)
        val publicKeySpec = X509EncodedKeySpec(encodedKey)
        val kf = KeyFactory.getInstance("RSA")
        val pkPublic = kf.generatePublic(publicKeySpec)
        val pkCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING")
        try {
            pkCipher.init(Cipher.ENCRYPT_MODE, pkPublic)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
            return null
        }

        return try {
            pkCipher.doFinal(this)
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
            null
        } catch (e: BadPaddingException) {
            e.printStackTrace()
            null
        }
    }

    @Throws(Exception::class)
    fun encryptAES(key: ByteArray, clear: ByteArray, iv: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec)
        return cipher.doFinal(clear)
    }

    @Throws(Exception::class)
    fun decryptAES(key: ByteArray, encrypted: ByteArray, iv: ByteArray): String? {
        val skeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec)
        val decrypted = cipher.doFinal(encrypted)
        return Base64.encodeToString(decrypted, Base64.DEFAULT)
    }

    fun ByteArray.signRSA(resources: Resources): ByteArray? {
        try {
            //byte[] sign = encryptRSA(hash);
            val instream = resources.openRawResource(R.raw.client_private_pkcs8_2)
            val encodedKey = ByteArray(instream.available())
            instream.read(encodedKey)
            val keySpec = PKCS8EncodedKeySpec(encodedKey)
            val kf = KeyFactory.getInstance("RSA")
            val pkPrivate = kf.generatePrivate(keySpec)
            val instance = Signature.getInstance("SHA1withRSA")
            instance.initSign(pkPrivate)
            instance.update(this)
            return instance.sign()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return null
    }
}