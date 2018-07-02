package com.nrs.nsnik.notes.view.fragments.dialogFragments


import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.nrs.nsnik.notes.MyApplication
import com.nrs.nsnik.notes.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_password_dialog.*
import timber.log.Timber


class PasswordDialogFragment : DialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private val default: String = "NA"
    private lateinit var value: String
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

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
        sharedPreferences = (activity?.applicationContext as MyApplication).sharedPreferences
        value = sharedPreferences.getString(resources?.getString(R.string.sharedPreferencePasswordKey), default)
        if (value == default) {
            passwordDialogCreate.text = resources?.getString(R.string.save)
            passwordDialogText.hint = resources?.getString(R.string.enterNewPassword)
        } else {
            passwordDialogCreate.text = resources?.getString(R.string.enter)
            passwordDialogText.hint = resources?.getString(R.string.dialogPasswordFieldHint)
        }
    }

    private fun listeners() {
        val resources = activity?.resources
        compositeDisposable.addAll(
                RxView.clicks(passwordDialogCreate).subscribe {
                    //TODO REFACTOR THIS
                    if (passwordDialogText.text.toString().isNotEmpty()) {
                        if (value == default) {
                            sharedPreferences.edit()
                                    .putString(resources?.getString(R.string.sharedPreferencePasswordKey), passwordDialogText.text.toString())
                                    .apply()
                            dismiss()
                        } else {
                            if (value == passwordDialogText.text.toString()) {
                                Timber.d("Valid")
                                dismiss()
                            } else {
                                passwordDialogText.error = resources?.getString(R.string.errorNopassword)
                            }
                        }
                    } else {
                        passwordDialogText.error = resources?.getString(R.string.errorNopassword)
                    }
                },
                RxView.clicks(passwordDialogCancel).subscribe { dismiss() },
                RxCompoundButton.checkedChanges(passwordDialogShowPassword).subscribe {
                    passwordDialogText.inputType = if (it) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_NUMBER_VARIATION_PASSWORD
                }
        )
    }

}
