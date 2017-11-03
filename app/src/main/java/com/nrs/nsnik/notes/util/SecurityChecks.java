/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier;

import java.security.MessageDigest;

import javax.inject.Inject;

import timber.log.Timber;

public class SecurityChecks {

    private static final String PLAY_STORE_APP_ID = "com.android.vending";
    private static final String APP_SIGNATURE = "MbCXSXhJfRu65ou6CeZLhGz+rnw=";

    private final Context mContext;

    @Inject
    SecurityChecks(@ApplicationQualifier Context context) {
        mContext = context;
    }

    /**
     * @return true if app developer signature matches
     * the given constant
     */
    public boolean checkAppSignature() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                byte[] signatureBytes = signature.toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signatureBytes);
                String currentSignature = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Timber.d(currentSignature);
                if (APP_SIGNATURE.equals(currentSignature)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return true if the application was
     * installed from play store
     */
    public boolean verifyInstaller() {
        final String installer = mContext.getPackageManager().getInstallerPackageName(mContext.getPackageName());
        return installer != null && installer.startsWith(PLAY_STORE_APP_ID);
    }

    /**
     * @param name the name of system property whose detail we require
     * @return the detail info about the property
     * @throws Exception exception like classNotFound or noSuchMethod
     */
    @NonNull
    private String getSystemProperty(String name) throws Exception {
        @SuppressLint("PrivateApi") Class systemPropertyClazz = Class.forName("android.os.SystemProperties");
        return (String) systemPropertyClazz.getMethod("get", new Class[]{String.class}).invoke(systemPropertyClazz, name);
    }

    /**
     * @return true if the device is emulator
     */
    public boolean checkEmulator() {
        try {
            boolean goldfish = getSystemProperty("ro.hardware").contains("goldfish");
            boolean emu = getSystemProperty("ro.kernel.qemu").length() > 0;
            boolean sdk = getSystemProperty("ro.product.model").equals("sdk");
            if (emu || goldfish || sdk) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return true if debugging is enabled
     */
    public boolean checkDebuggable() {
        return (mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

}
