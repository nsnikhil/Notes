package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.nrs.nsnik.notes.view.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LockFlowTest {

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, false, false)

    private var idlingResource: IdlingResource? = null

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putBoolean("bundleIntroWatched", true)
            // Clear any password set by previous tests so each test starts unlocked
            .remove("sharedPreferencePasswordKey")
            .remove("sharedPreferencePrivateKey")
            .remove("sharedPreferencePublicKey")
            .commit()
        activityRule.launchActivity(null)
        idlingResource = activityRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(idlingResource)
    }

    private fun navigateToNewNote() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
    }

    @Test
    fun lockIconIsDisplayedInNewNote() {
        navigateToNewNote()
        onView(withId(R.id.newNoteMenuLock)).check(matches(isDisplayed()))
    }

    @Test
    fun tapLockIconShowsPasswordDialog() {
        navigateToNewNote()
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Lock Test"), closeSoftKeyboard())
        onView(withId(R.id.newNoteMenuLock)).perform(click())
        onView(withId(R.id.passwordDialogText))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun passwordDialogHasCreateAndCancelButtons() {
        navigateToNewNote()
        onView(withId(R.id.newNoteMenuLock)).perform(click())
        onView(withId(R.id.passwordDialogCreate))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(withId(R.id.passwordDialogCancel))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDismissPasswordDialogNoLockChange() {
        navigateToNewNote()
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Dismiss Test"), closeSoftKeyboard())
        onView(withId(R.id.newNoteMenuLock)).perform(click())
        // Wait for DialogFragment to inflate and async decryptAndGet to complete
        Thread.sleep(2000)
        // Dismiss dialog via Cancel button
        onView(withId(R.id.passwordDialogCancel))
            .inRoot(isDialog())
            .perform(click())
        Thread.sleep(500)
        // Lock icon should still be visible (note remains unlocked)
        onView(withId(R.id.newNoteMenuLock)).check(matches(isDisplayed()))
    }

    @Test
    fun testLockTogglesTwice() {
        navigateToNewNote()
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Toggle Test"), closeSoftKeyboard())

        // First tap — opens password dialog, set password to lock
        onView(withId(R.id.newNoteMenuLock)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.passwordDialogTextOld))
            .inRoot(isDialog())
            .perform(replaceText("test1234"), closeSoftKeyboard())
        onView(withId(R.id.passwordDialogText))
            .inRoot(isDialog())
            .perform(replaceText("test1234"), closeSoftKeyboard())
        onView(withId(R.id.passwordDialogCreate))
            .inRoot(isDialog())
            .perform(click())
        Thread.sleep(1000)

        // Second tap — password already exists, so it toggles directly to unlock
        onView(withId(R.id.newNoteMenuLock)).perform(click())
        Thread.sleep(300)
        // Lock icon should still be visible after toggling twice
        onView(withId(R.id.newNoteMenuLock)).check(matches(isDisplayed()))
    }

    @Test
    fun testPasswordDialogHasInputField() {
        navigateToNewNote()
        onView(withId(R.id.newNoteMenuLock)).perform(click())
        Thread.sleep(500)
        // Verify the password input field exists and is displayed
        onView(withId(R.id.passwordDialogText))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        // Verify the TextInputLayout container is also displayed
        onView(withId(R.id.passwordDialogTextContainer))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
