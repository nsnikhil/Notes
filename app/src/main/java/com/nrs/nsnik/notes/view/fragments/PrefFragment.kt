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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.databinding.FragmentPreferencesBinding // Added ViewBinding import
import com.nrs.nsnik.notes.view.fragments.dialogFragments.PasswordDialogFragment
import hugo.weaving.DebugLog
import io.reactivex.disposables.CompositeDisposable
// import kotlinx.android.synthetic.main.fragment_preferences.* // Removed synthetic import


class PrefFragment : Fragment() {

    private var _binding: FragmentPreferencesBinding? = null // Added nullable binding variable
    private val binding get() = _binding!! // Non-null accessor

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPreferencesBinding.inflate(inflater, container, false) // Inflate binding
        return binding.root // Return binding root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeItems()
        initialize()
    }

    private fun getResourceString(id: Int): String? = activity?.resources?.getString(id)

    @DebugLog
    private fun initializeItems() {
        binding.prefItemChangePassword.text = getString( // Use binding
                getResourceString(R.string.preferenceChangeCode)!!,
                getResourceString(R.string.preferenceChangeCodeSummary)!!
        )
        binding.prefItemPrivacySendData.text = getString( // Use binding
                getResourceString(R.string.preferenceSendData)!!,
                getResourceString(R.string.preferenceSendDataSummary)!!
        )
        binding.prefItemPrivacyErrorReport.text = getString( // Use binding
                getResourceString(R.string.preferenceErrorReporting)!!,
                getResourceString(R.string.preferenceErrorReportingSummary)!!
        )
        binding.prefItemRestoreData.text = getString( // Use binding
                getResourceString(R.string.preferenceRestore)!!,
                getResourceString(R.string.preferenceRestoreSummary)!!
        )
        binding.prefItemBackupData.text = getString( // Use binding
                getResourceString(R.string.preferenceBackup)!!,
                getResourceString(R.string.preferenceBackupSummary)!!
        )
    }

    @SuppressLint("CheckResult")
    private fun initialize() {
        compositeDisposable.addAll(
                RxView.clicks(binding.prefItemChangePassword).subscribe { openPasswordDialog() }, // Use binding
                RxView.clicks(binding.prefItemBackupData).subscribe { exportDatabase() }, // Use binding
                RxView.clicks(binding.prefItemRestoreData).subscribe { importDatabase() }, // Use binding
                RxCompoundButton.checkedChanges(binding.prefItemDarkMode).subscribe { switchTheme(it) }, // Use binding
                RxCompoundButton.checkedChanges(binding.prefItemPrivacyErrorReport).subscribe { enableErrorReporting(it) }, // Use binding
                RxCompoundButton.checkedChanges(binding.prefItemPrivacySendData).subscribe { enableSendData(it) } // Use binding
        )
    }

    private fun getString(title: String, summary: String): Spanned {
        return Html.fromHtml("<font color='#FFFFFF'>$title</font> <br> <font color='#9E9E9E'>$summary</font>")
    }

    private fun openPasswordDialog() {
        val dialog = PasswordDialogFragment()
        dialog.setTargetFragment(this, 1954)
        dialog.show(fragmentManager, "changePassword")
    }

    private fun exportDatabase() {
        Toast.makeText(activity!!, "Sorry the feature is currently under development", Toast.LENGTH_LONG).show()
    }

    private fun importDatabase() {
        Toast.makeText(activity!!, "Sorry the feature is currently under development", Toast.LENGTH_LONG).show()
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

    override fun onDestroyView() { // Changed to onDestroyView
        super.onDestroyView()
        _binding = null // Set binding to null
        cleanUp() // Assuming cleanUp is for view-related disposables
    }

    override fun onDestroy() {
        super.onDestroy()
        // If cleanUp has non-view related disposables, it might still be needed here.
    }
}