/*
 * Copyright (C) 2017 nsnikhil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nrs.nsnik.notes.view.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * this fragments shows
 * info about the app and all the
 * 3rd party libraries used to make this app
 * and also the license info
 */

class AboutFragment : Fragment() {

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }

    private fun initialize() {
        aboutSourceCode.movementMethod = LinkMovementMethod.getInstance()
        aboutNikhil.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun listeners() {
        compositeDisposable.addAll(
                RxView.clicks(aboutLibraries).subscribe { startActivity(Intent(activity, OssLicensesMenuActivity::class.java)) },
                RxView.clicks(aboutLicense).subscribe { chromeCustomTab(activity?.resources?.getString(R.string.aboutLicenseUrl)!!) }
        )
    }

    /**
     * @param url display the url supplied im a chrome custom tab
     */
    private fun chromeCustomTab(url: String) {
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(activity!!, R.color.colorPrimary))
        builder.setSecondaryToolbarColor(ContextCompat.getColor(activity!!, R.color.colorPrimaryDark))
        builder.setExitAnimations(activity!!, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(activity!!, Uri.parse(url))
    }


    private fun cleanUp() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }
}
