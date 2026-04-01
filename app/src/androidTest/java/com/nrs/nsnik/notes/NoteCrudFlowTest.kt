package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class NoteCrudFlowTest {

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
    fun tapFabRevealsAddNoteButton() {
        onView(withId(R.id.fabAdd)).perform(click())
        // After FAB animation, the add-note container becomes visible
        onView(withId(R.id.fabAddNote)).check(matches(isDisplayed()))
    }

    @Test
    fun tapFabRevealsAddFolderButton() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).check(matches(isDisplayed()))
    }

    @Test
    fun tapAddNoteOpensNewNoteFragment() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.newNoteContent)).check(matches(isDisplayed()))
    }

    @Test
    fun enterNoteTitleAndContent() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Test Note Title"), closeSoftKeyboard())
        onView(withId(R.id.newNoteContent))
            .perform(replaceText("Test note content body"), closeSoftKeyboard())
        onView(withId(R.id.newNoteTitle)).check(matches(withText("Test Note Title")))
        onView(withId(R.id.newNoteContent)).check(matches(withText("Test note content body")))
    }

    @Test
    fun saveNoteViaMenuAndReturnToList() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Save Test Note"), closeSoftKeyboard())
        onView(withId(R.id.newNoteContent))
            .perform(replaceText("Content for save test"), closeSoftKeyboard())
        onView(withId(R.id.newNoteMenuSave)).perform(click())
        // After save, should return to list
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
    }

    @Test
    fun backFromNewNoteReturnsToList() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))
        pressBack()
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
