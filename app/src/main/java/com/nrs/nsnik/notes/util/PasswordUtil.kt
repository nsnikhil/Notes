/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.nio.charset.Charset
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class PasswordUtil {

    companion object {

        private const val default: String = "NA"
        private const val defaultPrivateKey = "NA"
        private const val defaultPublicKey = "NA"

        private fun passwordExists(sharedPreferences: SharedPreferences, context: Context): Boolean {
            return sharedPreferences.getString(context.resources?.getString(R.string.sharedPreferencePasswordKey), default) != default
        }

        fun checkLock(sharedPreferences: SharedPreferences, context: Context, fragmentManager: FragmentManager, tag: String): Boolean {
            if (!passwordExists(sharedPreferences, context)) {
                showPasswordDialog(fragmentManager, tag)
                return false
            }
            return true
        }

        private fun showPasswordDialog(fragmentManager: FragmentManager, tag: String) {
            PasswordDialogFragment().show(fragmentManager, tag)
        }

        fun encryptAndGet(plainText: String, sharedPreferences: SharedPreferences, context: Context): MutableLiveData<String> {
            val liveData = MutableLiveData<String>()

            val single: Single<String> = Single.fromCallable {
                return@fromCallable encrypt(plainText, sharedPreferences, context)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            single.subscribe(object : SingleObserver<String> {
                override fun onSuccess(t: String) {
                    if (t.isNotEmpty() && t != default)
                        liveData.value = t
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    Timber.d(e)
                }
            })
            return liveData
        }

        fun decryptAndGet(sharedPreferences: SharedPreferences, context: Context): MutableLiveData<String> {
            val liveData = MutableLiveData<String>()
            if (!passwordExists(sharedPreferences, context)) {
                liveData.value = default
                return liveData
            }

            val single: Single<String> = Single.fromCallable {
                return@fromCallable decrypt(sharedPreferences.getString(context.resources?.getString(R.string.sharedPreferencePasswordKey), default),
                        sharedPreferences, context)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            single.subscribe(object : SingleObserver<String> {
                override fun onSuccess(t: String) {
                    liveData.value = t
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    Timber.d(e)
                }
            })
            return liveData
        }

        private fun encrypt(plainText: String, sharedPreferences: SharedPreferences, context: Context): String {
            return getEncryptedString(getEncryptedKey(getCipher(), getPrivateKey(sharedPreferences, context), plainText))
        }

        private fun decrypt(cipherText: String, sharedPreferences: SharedPreferences, context: Context): String {
            return String(getDecryptedKey(getCipher(), getPublicKey(sharedPreferences, context),
                    getDecryptedByteArray(cipherText)), Charset.forName("UTF-8"))
        }

        private fun getEncryptedString(encrypted: ByteArray): String {
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }

        private fun getDecryptedByteArray(encodedString: String): ByteArray {
            return Base64.decode(encodedString, Base64.NO_WRAP)
        }

        private fun getKeyPair(): KeyPair {
            return buildKeyPair()
        }

        private fun getPublicKey(sharedPreferences: SharedPreferences, context: Context): PublicKey {
            val publicKey = sharedPreferences.getString(context.resources?.getString(R.string.sharedPreferencePublicKey), defaultPublicKey)
            val key: PublicKey
            val keyFactory = KeyFactory.getInstance("RSA")

            if (publicKey != defaultPrivateKey) key = keyFactory.generatePublic(X509EncodedKeySpec(Base64.decode(publicKey, Base64.NO_WRAP)))
            else {
                val keyPair: KeyPair = getKeyPair()
                key = keyPair.public
                saveKeys(sharedPreferences, context, keyPair.private, key)
            }
            return key
        }

        private fun getPrivateKey(sharedPreferences: SharedPreferences, context: Context): PrivateKey {

            val privateKey = sharedPreferences.getString(context.resources?.getString(R.string.sharedPreferencePrivateKey), defaultPrivateKey)
            val key: PrivateKey
            val keyFactory = KeyFactory.getInstance("RSA")

            if (privateKey != defaultPrivateKey) key = keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.NO_WRAP)))
            else {
                val keyPair: KeyPair = getKeyPair()
                key = keyPair.private
                saveKeys(sharedPreferences, context, key, keyPair.public)
            }
            return key
        }

        private fun saveKeys(sharedPreferences: SharedPreferences, context: Context, privateKey: PrivateKey, publicKey: PublicKey) {
            sharedPreferences.edit()
                    .putString(context.resources?.getString(R.string.sharedPreferencePrivateKey), Base64.encodeToString(privateKey.encoded, Base64.NO_WRAP))
                    .putString(context.resources?.getString(R.string.sharedPreferencePublicKey), Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP))
                    .apply()
        }

        private fun buildKeyPair(): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(1024, SecureRandom())
            return keyPairGenerator.genKeyPair()
        }

        private fun getCipher(): Cipher {
            return Cipher.getInstance("RSA")
        }

        private fun getEncryptedKey(cipher: Cipher, privateKey: PrivateKey, plainText: String): ByteArray {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey)
            return cipher.doFinal(plainText.toByteArray(Charset.forName("UTF-8")))
        }

        private fun getDecryptedKey(cipher: Cipher, publicKey: PublicKey, encrypted: ByteArray): ByteArray {
            cipher.init(Cipher.DECRYPT_MODE, publicKey)
            return cipher.doFinal(encrypted)
        }
    }
}