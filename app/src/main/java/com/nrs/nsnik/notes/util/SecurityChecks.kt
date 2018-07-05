/*
 *     Credit Card Security V1  Copyright (C) 2018  sid-sun
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


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Base64
import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

class SecurityChecks @Inject internal constructor(@param:ApplicationQualifier private val mContext: Context) {

    /**
     * @return true if app developer signature matches
     * the given constant
     */
    fun checkAppSignature(): Boolean {
        try {
            @SuppressLint("PackageManagerGetSignatures") val packageInfo = mContext.packageManager.getPackageInfo(mContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in packageInfo.signatures) {
                val signatureBytes = signature.toByteArray()
                val md = MessageDigest.getInstance("SHA")
                md.update(signatureBytes)
                val currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Timber.d(currentSignature)
                if (APP_SIGNATURE == currentSignature) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * @return true if the application was
     * installed from play store
     */
    fun verifyInstaller(): Boolean {
        val installer = mContext.packageManager.getInstallerPackageName(mContext.packageName)
        return installer != null && installer.startsWith(PLAY_STORE_APP_ID)
    }

    /**
     * @param name the name of system property whose detail we require
     * @return the detail info about the property
     * @throws Exception exception like classNotFound or noSuchMethod
     */
    @Throws(Exception::class)
    private fun getSystemProperty(name: String): String {
        @SuppressLint("PrivateApi") val systemPropertyClazz = Class.forName("android.os.SystemProperties")
        return systemPropertyClazz.getMethod("get", *arrayOf<Class<*>>(String::class.java)).invoke(systemPropertyClazz, name) as String
    }

    /**
     * @return true if the device is emulator
     */
    fun checkEmulator(): Boolean {
        try {
            val goldfish = getSystemProperty("ro.hardware").contains("goldfish")
            val emu = getSystemProperty("ro.kernel.qemu").length > 0
            val sdk = getSystemProperty("ro.product.model") == "sdk"
            if (emu || goldfish || sdk) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * @return true if debugging is enabled
     */
    fun checkDebuggable(): Boolean {
        return mContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    companion object {

        private val PLAY_STORE_APP_ID = "com.android.vending"
        private val APP_SIGNATURE = "MbCXSXhJfRu65ou6CeZLhGz+rnw="
    }

}
