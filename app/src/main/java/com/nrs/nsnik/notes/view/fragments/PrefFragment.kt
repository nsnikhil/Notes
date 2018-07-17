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

package com.nrs.nsnik.notes.view.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment
import hugo.weaving.DebugLog
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_preferences.*


class PrefFragment : Fragment() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeItems()
        initialize()
    }

    @DebugLog
    private fun initializeItems() {
        prefItemChangePassword.text = getString(
                activity?.resources?.getString(R.string.preferenceChangeCode)!!,
                activity?.resources?.getString(R.string.preferenceChangeCodeSummary)!!
        )
        prefItemPrivacySendData.text = getString(
                activity?.resources?.getString(R.string.preferenceSendData)!!,
                activity?.resources?.getString(R.string.preferenceSendDataSummary)!!
        )
        prefItemPrivacyErrorReport.text = getString(
                activity?.resources?.getString(R.string.preferenceErrorReporting)!!,
                activity?.resources?.getString(R.string.preferenceErrorReportingSummary)!!
        )
        prefItemRestoreData.text = getString(
                activity?.resources?.getString(R.string.preferenceRestore)!!,
                activity?.resources?.getString(R.string.preferenceRestoreSummary)!!
        )
        prefItemBackupData.text = getString(
                activity?.resources?.getString(R.string.preferenceBackup)!!,
                activity?.resources?.getString(R.string.preferenceBackupSummary)!!
        )
    }

    @SuppressLint("CheckResult")
    private fun initialize() {
        compositeDisposable.addAll(
                RxView.clicks(prefItemChangePassword).subscribe { openPasswordDialog() },
                RxView.clicks(prefItemBackupData).subscribe { exportDatabase() },
                RxView.clicks(prefItemRestoreData).subscribe { importDatabase() },
                RxCompoundButton.checkedChanges(prefItemDarkMode).subscribe { switchTheme(it) },
                RxCompoundButton.checkedChanges(prefItemPrivacyErrorReport).subscribe { enableErrorReporting(it) },
                RxCompoundButton.checkedChanges(prefItemPrivacySendData).subscribe { enableSendData(it) }
        )
    }

    private fun getString(title: String, summary: String): Spanned {
        return Html.fromHtml("<font color='#000000'>$title</font> <br> <font color='#9E9E9E'>$summary</font>", Html.FROM_HTML_MODE_LEGACY)
    }

    private fun openPasswordDialog() {
        val dialog = PasswordDialogFragment()
        dialog.setTargetFragment(this, 1954)
        dialog.show(fragmentManager, "changePassword")
    }

    private fun exportDatabase() {

    }

    private fun importDatabase() {

    }

    private fun switchTheme(darkMode: Boolean) {

    }

    private fun enableErrorReporting(enabled: Boolean) {

    }

    private fun enableSendData(enabled: Boolean) {

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