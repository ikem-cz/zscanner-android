package cz.ikem.dci.zscanner.biometrics

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat.startActivity
import cz.ikem.dci.zscanner.*
import cz.ikem.dci.zscanner.screen_splash_login.SplashLoginActivity
import cz.ikem.dci.zscanner.webservices.HttpClient.application
import kotlinx.coroutines.withContext
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


// Compatible with Android 6+
class BiometricsKey(private val keyName: String) {


    private val TAG = BiometricsKey::class.java.simpleName

    @Volatile
    private var generating: Boolean = false

    val keyPair: KeyPair?
        get() {
            if (generating) return null

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(keyName)) {
                // Get public key
                val publicKey = keyStore.getCertificate(keyName).publicKey
                // Get private key
                val privateKey = keyStore.getKey(keyName, null) as PrivateKey
                // Return a key pair
                return KeyPair(publicKey, privateKey)
            }
            return null
        }


    fun generateKeyPair() {
        generating = true
        try {

            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")

            val builder = KeyGenParameterSpec.Builder(
                    keyName,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY or KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                    .setKeySize(4096)
                    .setDigests(
                            KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA384,
                            KeyProperties.DIGEST_SHA512
                    )
                    // Require the user to authenticate with a biometric to authorize every use of the key
                    .setUserAuthenticationRequired(true)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

            // Generated keys will be invalidated if the biometric templates are added more to user device
            if (Build.VERSION.SDK_INT >= 24) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }

            keyPairGenerator.initialize(builder.build())
            keyPairGenerator.generateKeyPair()
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to generate biometrics key", exception)
        } finally {
            generating = false
        }

        //TODO: https://developer.android.com/training/articles/security-key-attestation
    }


    //TODO: Sign (based on the BiometricKeyDecrypt)

    fun verify(data: ByteBuffer, signature: ByteBuffer): Boolean {
        val kp = keyPair ?: return false

        val arr = ByteArray(signature.limit())
        signature.get(arr, 0, signature.limit())

        val sign = Signature.getInstance("SHA384withRSA")
        sign.initVerify(kp.public)

        sign.update(data)
        return sign.verify(arr)
    }

    fun encrypt(plaintext: ByteBuffer, cyphertext: ByteBuffer): Boolean {
        val kp = keyPair ?: return false

        val plaintext_arr = ByteArray(plaintext.limit())
        plaintext.get(plaintext_arr, 0, plaintext.limit())

        // Prepare symmetric cypher AES-256
        val generator = KeyGenerator.getInstance("AES")
        generator.init(256)
        val secretKey = generator.generateKey()

        // Encrypt plaintext by AES secret key
        val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val cyphertext_arr = aesCipher.doFinal(plaintext_arr)
        val cyphertext_iv = aesCipher.iv

        // Encrypt AES secret key using RSA public key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(KeyProperties.PURPOSE_ENCRYPT, kp.public)
        val encryptedkey_arr = cipher.doFinal(secretKey.encoded)

        // Construct the cyphertext
        cyphertext.putInt(1) // Version
        cyphertext.putInt(encryptedkey_arr.size) // Size
        cyphertext.put(encryptedkey_arr)
        cyphertext.put(cyphertext_iv)
        cyphertext.put(cyphertext_arr)
        cyphertext.flip()

        return true
    }


    fun decrypt(cyphertext: ByteBuffer): BiometricKeyDecrypt? {

        try {
            val kp = keyPair // ?: return null
            kp?.let {
                return (BiometricKeyDecrypt(kp, cyphertext))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception: $e")
        }
        return null
    }
}


class BiometricKeyDecrypt(var keyPair: KeyPair, cyphertext: ByteBuffer) {

    val encryptedkey_arr: ByteArray
    val cyphertext_iv: ByteArray
    val cyphertext_arr: ByteArray

    init {
        val version = cyphertext.getInt()
        if (version != 1) {
            encryptedkey_arr = byteArrayOf()
            cyphertext_iv = byteArrayOf()
            cyphertext_arr = byteArrayOf()
            throw GeneralSecurityException()
        }

        val encryptedkey_len = cyphertext.getInt()
        encryptedkey_arr = ByteArray(encryptedkey_len)
        cyphertext.get(encryptedkey_arr, 0, encryptedkey_arr.size)

        cyphertext_iv = ByteArray(12)
        cyphertext.get(cyphertext_iv, 0, cyphertext_iv.size)

        cyphertext_arr = ByteArray(cyphertext.limit() - cyphertext.position())
        cyphertext.get(cyphertext_arr, 0, cyphertext_arr.size)
    }

    fun prompt(biometrics_prompt: BiometricPrompt, info: BiometricPrompt.PromptInfo, onExceptionCallback: (keyPermanentlyInvalidated: Boolean) -> Unit) {
        val aesCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

        try {
            aesCipher.init(KeyProperties.PURPOSE_DECRYPT, keyPair.private)
            biometrics_prompt.authenticate(info, BiometricPrompt.CryptoObject(aesCipher))

        } catch (ex: Exception) {
            when (ex) {
                is KeyPermanentlyInvalidatedException -> { // user changed fingerprints on the device

                    val keyStore = KeyStore.getInstance("AndroidKeyStore")
                    keyStore.load(null)
                    keyStore.deleteEntry(BIOMETRIC_KEY_NAME)

                    val sharedPreferences = application.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
                    sharedPreferences.edit()
                            .remove(PREF_USERNAME)
                            .remove(PREF_ACCESS_TOKEN)
                            .apply()
                    onExceptionCallback(true)
                }
                else -> throw ex
            }
        }
    }


    fun final(crypto_object: BiometricPrompt.CryptoObject?, plaintext: ByteBuffer): Boolean {
        val cipher = crypto_object?.cipher ?: return false

        try {
            val decryptedKey = cipher.doFinal(encryptedkey_arr)
            val secretKey = SecretKeySpec(decryptedKey, 0, decryptedKey.size, "AES")

            val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, cyphertext_iv) // Must be 128 (length of authorization tag)
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            plaintext.put(aesCipher.doFinal(cyphertext_arr))
        } catch (e: GeneralSecurityException) {
            plaintext.position(0)
            return false
        }

        if (plaintext.position() == 0) return false

        plaintext.flip()
        return true
    }

    companion object {
        const val TAG = "BiometricKeyDecrypt"
    }
}
