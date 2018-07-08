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

package com.nrs.nsnik.notes.view.fragments.dialogFragments


import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding2.view.RxView
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import com.nrs.nsnik.notes.util.events.PasswordEvent
import com.nrs.nsnik.notes.view.fragments.ListFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_password_dialog.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber


class PasswordDialogFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val default: String = "NA"
    private lateinit var value: String
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var itemType: ListFragment.ItemType
    private var itemPosition: Int = -1
    private lateinit var eventType: ListFragment.EventType

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_password_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        listeners()
    }

    private fun initialize() {
        val resources = activity?.resources

        if (arguments != null) {
            itemType = ListFragment.ItemType.values()[arguments?.getInt(resources?.getString(R.string.bundleItemType), 0)!!]
            itemPosition = arguments?.getInt(resources?.getString(R.string.bundleItemPosition), -1)!!
            eventType = ListFragment.EventType.values()[arguments?.getInt(resources?.getString(R.string.bundleItemEvent), 0)!!]
        }

        sharedPreferences = (activity?.applicationContext as MyApplication).sharedPreferences
        value = sharedPreferences.getString(resources?.getString(R.string.sharedPreferencePasswordKey), default)

        Timber.d(targetRequestCode.toString())

        setValues(value != default)
    }

    private fun setValues(hasPassword: Boolean) {
        val resources = activity?.resources

        if (!hasPassword) {
            passwordDialogTextContainerOld.visibility = View.VISIBLE
            passwordDialogCreate.text = resources?.getString(R.string.save)
            setFields(resources?.getString(R.string.reEnterNewPassword), resources?.getString(R.string.enterNewPassword))
        } else {
            if (targetRequestCode == 1954) {
                passwordDialogTextContainerOld.visibility = View.VISIBLE
                passwordDialogCreate.text = resources?.getString(R.string.save)
                setFields(resources?.getString(R.string.enterNewPassword), resources?.getString(R.string.oldPassword))
            } else {
                passwordDialogTextContainerOld.visibility = View.GONE
                passwordDialogCreate.text = resources?.getString(R.string.enter)
                passwordDialogText.hint = resources?.getString(R.string.password)
            }
        }
    }

    private fun setFields(newHint: String?, oldHint: String?) {
        passwordDialogText.hint = newHint
        passwordDialogTextOld.hint = oldHint
    }

    private fun listeners() {
        compositeDisposable.addAll(
                RxView.clicks(passwordDialogCreate).subscribe {
                    if (isNotEmpty(passwordDialogText))
                        when {
                            value == default -> addNewPassWord()
                            targetRequestCode == 1954 -> changePassword()
                            else -> isPasswordCorrect(passwordDialogText)
                        }
                },
                RxView.clicks(passwordDialogCancel).subscribe {
                    dismiss()
                }
        )
    }

    private fun isNotEmpty(editText: TextInputEditText): Boolean {
        return if (editText.text.toString().isNotEmpty()) true
        else {
            editText.error = activity?.resources?.getString(R.string.errorNopassword)
            false
        }
    }

    private fun isPasswordCorrect(editText: TextInputEditText): Boolean {
        return if (value == editText.text.toString()) {
            dismiss()
            EventBus.getDefault().post(PasswordEvent(itemType, itemPosition, eventType))
            true
        } else {
            editText.error = activity?.resources?.getString(R.string.errorNopassword)
            false
        }
    }

    private fun addNewPassWord() {
        if (passwordDialogText.text.toString() == passwordDialogTextOld.text.toString()) {
            sharedPreferences.edit().putString(activity?.resources?.getString(R.string.sharedPreferencePasswordKey), passwordDialogText.text.toString()).apply()
            dismiss()
        } else
            passwordDialogText.error = activity?.resources?.getString(R.string.errorPasswordNoMatch)
    }

    private fun changePassword() {
        if (value == passwordDialogTextOld.text.toString()) {
            sharedPreferences.edit().putString(activity?.resources?.getString(R.string.sharedPreferencePasswordKey), passwordDialogText.text.toString()).apply()
            dismiss()
        } else {
            passwordDialogTextOld.error = activity?.resources?.getString(R.string.errorPasswordNoMatch)
            passwordDialogText.setText("")
        }

    }

}
