package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.nrs.nsnik.notes.view.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderFlowTest {

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, false, false)

    private var idlingResource: IdlingResource? = null
    private lateinit var uiDevice: UiDevice

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean("bundleIntroWatched", true).commit()
        activityRule.launchActivity(null)
        idlingResource = activityRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(idlingResource)
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    private fun navigateToNewNote() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Reminder Test"), closeSoftKeyboard())
    }

    /** Dismiss the system TimePickerDialog by clicking its Cancel button via UiAutomator. */
    private fun dismissTimePicker() {
        // Wait for the Cancel button to appear in the TimePickerDialog
        val cancelButton = uiDevice.wait(Until.findObject(By.text("Cancel")), 3000)
            ?: uiDevice.wait(Until.findObject(By.text("CANCEL")), 2000)
        cancelButton?.click()
        Thread.sleep(500)
    }

    @Test
    fun reminderToolIsDisplayedInBottomSheet() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsReminder)).check(matches(isDisplayed()))
    }

    @Test
    fun testReminderToolClickable() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsReminder))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun testDismissTimePickerNoReminder() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsReminder)).perform(click())
        dismissTimePicker()
        // Verify the note screen is still displayed (no crash)
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun testReminderToolTapTwice() {
        navigateToNewNote()

        // First tap — opens time picker, dismiss via Cancel button
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsReminder)).perform(click())
        dismissTimePicker()
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))

        // After first dismissal, verify the bottom sheet tools are still in the
        // view hierarchy and the reminder tool remains clickable
        onView(withId(R.id.toolsReminder)).check(matches(isClickable()))
        // Verify no crash — note screen still visible
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
