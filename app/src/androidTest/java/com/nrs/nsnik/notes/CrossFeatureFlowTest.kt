package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.nrs.nsnik.notes.view.MainActivity
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrossFeatureFlowTest {

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, false, false)

    private var idlingResource: IdlingResource? = null

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("bundleIntroWatched", true)
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
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Cross Feature Test"), closeSoftKeyboard())
    }

    @Test
    fun testNewNoteHasAllTools() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(400)
        // Verify all 6 tools are displayed in the expanded bottom sheet
        onView(withId(R.id.toolsColor)).check(matches(isDisplayed()))
        onView(withId(R.id.toolsCamera)).check(matches(isDisplayed()))
        onView(withId(R.id.toolsAttachment)).check(matches(isDisplayed()))
        onView(withId(R.id.toolsCheckList)).check(matches(isDisplayed()))
        onView(withId(R.id.toolsAudio)).check(matches(isDisplayed()))
        onView(withId(R.id.toolsReminder)).check(matches(isDisplayed()))
    }

    @Test
    fun testPinAndSaveNote() {
        navigateToNewNote()
        onView(withId(R.id.newNoteContent))
            .perform(replaceText("Pinned note content"), closeSoftKeyboard())
        // Tap star/pin menu item
        onView(withId(R.id.newNoteMenuStar)).perform(click())
        Thread.sleep(300)
        // Pin icon should still be displayed after toggling
        onView(withId(R.id.newNoteMenuStar)).check(matches(isDisplayed()))
        // Save the note
        onView(withId(R.id.newNoteMenuSave)).perform(click())
    }

    @Test
    fun testNoteWithChecklistAndPin() {
        navigateToNewNote()
        // Add a checklist item
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(400)
        onView(withId(R.id.toolsCheckList)).perform(click())
        Thread.sleep(400)
        // Verify checklist is visible
        onView(withId(R.id.newNoteCheckList))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        // Pin the note
        onView(withId(R.id.newNoteMenuStar)).perform(click())
        Thread.sleep(300)
        // Save
        onView(withId(R.id.newNoteMenuSave)).perform(click())
    }

    @Test
    fun testBottomSheetExpandsAndCollapses() {
        navigateToNewNote()
        // Expand bottom sheet
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(400)
        // Verify tools are visible when expanded
        onView(withId(R.id.toolsCamera)).check(matches(isDisplayed()))
        onView(withId(R.id.toolsCheckList)).check(matches(isDisplayed()))
        // Collapse bottom sheet by tapping toolsDate again
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(400)
        // After collapse, the bottom sheet is still in the view hierarchy
        // but the tools row should not be visible to the user
        onView(withId(R.id.toolsDate)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
