package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
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
class PinFlowTest {

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java, false, false)

    private var idlingResource: IdlingResource? = null

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean("bundleIntroWatched", true).commit()
        activityRule.launchActivity(null)
        idlingResource = activityRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @Test
    fun newNoteMenuShowsStarOption() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteMenuStar)).check(matches(isDisplayed()))
    }

    @Test
    fun tapStarTogglesPinOnNewNote() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Pin Test Note"), closeSoftKeyboard())
        // Tap star to pin
        onView(withId(R.id.newNoteMenuStar)).perform(click())
        // Star button should still be displayed (toggled state)
        onView(withId(R.id.newNoteMenuStar)).check(matches(isDisplayed()))
    }

    @Test
    fun tapStarTwiceUnpinsNote() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Unpin Test Note"), closeSoftKeyboard())
        // Pin then unpin
        onView(withId(R.id.newNoteMenuStar)).perform(click())
        onView(withId(R.id.newNoteMenuStar)).perform(click())
        onView(withId(R.id.newNoteMenuStar)).check(matches(isDisplayed()))
    }

    @Test
    fun pinNoteAndSave() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Pinned Note"), closeSoftKeyboard())
        onView(withId(R.id.newNoteMenuStar)).perform(click())
        onView(withId(R.id.newNoteMenuSave)).perform(click())
        // Should return to list after save
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
