/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
