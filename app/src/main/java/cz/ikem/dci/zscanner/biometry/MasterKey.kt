package cz.ikem.dci.zscanner.biometry

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import java.lang.Thread.sleep
import java.nio.ByteBuffer
import java.security.*
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


class MasterKey(val biometrics: Biometrics) {

    private val TAG = MasterKey::class.java.simpleName

    val keyName = "zScanner Master Key"

    fun getKeyPair(): KeyPair? {
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
        while (biometrics.getBiometryState() != Biometrics.State.READY) {
            sleep(5000)
            Log.w(TAG, "Biometrics is not available, cannot generate the master key, will retry in 5 sec.")
        }

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

        //TODO: https://developer.android.com/training/articles/security-key-attestation
    }


    fun sign(data: ByteBuffer, signature: ByteBuffer): Long {

        val keyPair = getKeyPair()!!

        val sign = Signature.getInstance("SHA384withRSA")
        sign.initSign(keyPair.private)

        val lock = ReentrantLock()
        val condition = lock.newCondition()
        lock.lock()

        biometrics.displayBiometricPrompt(FingerprintManagerCompat.CryptoObject(sign)) {
            try {
                if (it != null) {
                    val s = it.signature!!
                    s.update(data)
                    val x = s.sign()
                    signature.put(x)
                }
            }
            finally {
                lock.lock()
                condition.signalAll()
                lock.unlock()
            }
        }

        condition.await()
        lock.unlock()

        if (signature.position() == 0) return 5 //CKR_GENERAL_ERROR
        signature.flip()

        return 0 //CKR_OK
    }

    fun verify(data: ByteBuffer, signature:ByteBuffer): Long {
        val keyPair = getKeyPair()!!
        val arr = ByteArray(signature.limit())
        signature.get(arr, 0, signature.limit())

        val sign = Signature.getInstance("SHA384withRSA")
        sign.initVerify(keyPair.public)

        sign.update(data)
        val result = sign.verify(arr)
        if (result) {
            return 0 //CKR_OK
        } else {
            return 5 //CKR_GENERAL_ERROR
        }
    }

    fun encrypt(plaintext: ByteBuffer, cyphertext:ByteBuffer): Long {
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

        val keyPair = getKeyPair()!!

        // Encrypt AES secret key using RSA public key
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(KeyProperties.PURPOSE_ENCRYPT, keyPair.public)
        val encryptedkey_arr = cipher.doFinal(secretKey.encoded)

        // Construct the cyphertext
        cyphertext.putInt(1) // Version
        cyphertext.putInt(encryptedkey_arr.size) // Size
        cyphertext.put(encryptedkey_arr)
        cyphertext.put(cyphertext_iv)
        cyphertext.put(cyphertext_arr)
        cyphertext.flip()

        return 0 //CKR_OK
    }

    fun decrypt(cyphertext: ByteBuffer, plaintext:ByteBuffer): Long {
        val version = cyphertext.getInt()
        if (version != 1) return 1 // Error

        val encryptedkey_len = cyphertext.getInt()
        val encryptedkey_arr = ByteArray(encryptedkey_len)
        cyphertext.get(encryptedkey_arr, 0, encryptedkey_arr.size)

        val cyphertext_iv = ByteArray(12)
        cyphertext.get(cyphertext_iv, 0, cyphertext_iv.size)

        val cyphertext_arr = ByteArray(8 + cyphertext.limit() - cyphertext.position())
        cyphertext.get(cyphertext_arr, 0, cyphertext_arr.size - 8)

        val keyPair = getKeyPair()!!

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(KeyProperties.PURPOSE_DECRYPT, keyPair.private)

        val lock = ReentrantLock()
        val condition = lock.newCondition()
        lock.lock()

        biometrics.displayBiometricPrompt(FingerprintManagerCompat.CryptoObject(cipher)) {
            try {
                if (it != null) {
                    val c = it.cipher!!
                    val decryptedKey = c.doFinal(encryptedkey_arr)
                    val secretKey = SecretKeySpec(decryptedKey, 0, decryptedKey.size, "AES")

                    val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
                    val spec = GCMParameterSpec(128, cyphertext_iv) // Must be 128 (length of authorization tag)
                    aesCipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                    plaintext.put(
                        aesCipher.doFinal(cyphertext_arr)
                    )
                }
            }

            catch (e: GeneralSecurityException) {
                plaintext.position(0)
            }

            finally {
                lock.lock()
                condition.signalAll()
                lock.unlock()
            }
        }
        condition.await()
        lock.unlock()

        if (plaintext.position() == 0) return 5 //CKR_GENERAL_ERROR
        plaintext.flip()

        return 0 //CKR_OK
    }

}
