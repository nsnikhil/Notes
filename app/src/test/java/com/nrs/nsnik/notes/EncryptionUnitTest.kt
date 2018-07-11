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

package com.nrs.nsnik.notes

import org.junit.Test
import java.nio.charset.Charset
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


class EncryptionUnitTest {

    @Test
    fun test() {

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(4096, SecureRandom())
        val keyPair: KeyPair = keyPairGenerator.genKeyPair()

        val privateKey: PrivateKey = keyPair.private
        val plainText: String = "19541826"
        val publicKey: PublicKey = keyPair.public


        val encodedPrivateKey: ByteArray = Base64.getEncoder().encode(privateKey.encoded)
        val encodedPublicKey: ByteArray = Base64.getEncoder().encode(publicKey.encoded)

        val encodedPrivateKeyFormat = privateKey.format
        val encodedPublicKeyFormat = publicKey.format

        val privateKeyString = Base64.getEncoder().encodeToString(encodedPrivateKey)
        val publicKeyString = Base64.getEncoder().encodeToString(encodedPublicKey)


        val keyFactory = KeyFactory.getInstance("RSA")

        val privateKeySpec: PKCS8EncodedKeySpec = PKCS8EncodedKeySpec(encodedPrivateKey)
        val privateKey2: RSAPrivateKey = keyFactory.generatePrivate(privateKeySpec) as RSAPrivateKey

        val publicKeySpec: X509EncodedKeySpec = X509EncodedKeySpec(encodedPublicKey)
        val publicKey2: RSAPublicKey = keyFactory.generatePublic(publicKeySpec) as RSAPublicKey

        assert(privateKey == privateKey2)
        assert(publicKey == publicKey2)

        val cipher: Cipher = Cipher.getInstance("RSA")

        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        val encrypted: ByteArray = cipher.doFinal(plainText.toByteArray(Charset.forName("UTF-8")))

        val store: String = Base64.getEncoder().encodeToString(encrypted)
        val take: ByteArray = Base64.getDecoder().decode(store)

        cipher.init(Cipher.DECRYPT_MODE, publicKey)

        val decrypted: ByteArray = cipher.doFinal(take)

        val encryptedString: String = Base64.getEncoder().encodeToString(encrypted)
        val decryptedString: String = String(decrypted, Charset.forName("UTF-8"))

        println(encryptedString)
        println(decryptedString)

        assert(plainText.contentEquals(decryptedString))

    }

}