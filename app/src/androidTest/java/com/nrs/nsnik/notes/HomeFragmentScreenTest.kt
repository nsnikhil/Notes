package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.nrs.nsnik.notes.view.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeFragmentScreenTest {
    @Rule
    var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)
    private var mIdlingResource: IdlingResource? = null

    @Before
    fun registerIdlingResource() {
        mIdlingResource = mActivityTestRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @Test
    fun openFabTest() {
        onView(withId(R.id.fabAdd)).perform(click())
    }

    @Test
    fun openAddNotesActivityTest() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).check(matches(isDisplayed()))
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun openAddFolderDialogTest() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).check(matches(isDisplayed()))
        onView(withId(R.id.fabAddFolder)).perform(click())
        onView(withId(R.id.dialogFolderName)).check(matches(isDisplayed()))
    }

    @Test
    fun addNewFolderTest() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).check(matches(isDisplayed()))
        onView(withId(R.id.fabAddFolder)).perform(click())
        onView(withId(R.id.dialogFolderName)).check(matches(isDisplayed()))
        onView(withId(R.id.dialogFolderName)).perform(typeText(TEST_FOLDER_NAME))
        onView(withId(R.id.dialogFolderCreate)).perform(click())
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
        onView(withId(R.id.commonList)).check(matches(hasDescendant(withText(TEST_FOLDER_NAME))))
    }

    @Test
    fun addNewNoteTest() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).check(matches(isDisplayed()))
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.newNoteTitle)).perform(typeText(TEMP_NOTE_TITLE))
        onView(withId(R.id.newNoteContent)).perform(typeText(TEMP_NOTE_BODY), closeSoftKeyboard())
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
        onView(withId(R.id.commonList)).check(matches(hasDescendant(withText(TEMP_NOTE_TITLE))))
    }


    @After
    fun unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource)
        }
    }

    companion object {

        private val TEST_FOLDER_NAME = "folder"
        private val TEMP_NOTE_TITLE = "title"
        private val TEMP_NOTE_TITLE_UPDATE = "update"
        private val TEMP_NOTE_BODY = "body"
    }

}
