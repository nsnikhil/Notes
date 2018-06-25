package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.nrs.nsnik.notes.view.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListFragmentScreenTest {
    @Rule
    var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)
    private var mIdlingResource: IdlingResource? = null

    @Before
    fun registerIdlingResource() {
        mIdlingResource = mActivityTestRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    /**
     * TODO THIS IS A FLAKY TEST REPLACE ONCE YOU MAKE A MATCHER FOR VIEW-HOLDER
     */
    @Test
    fun openNotesActivityTest() {
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
        onView(withId(R.id.commonList)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEMP_NOTE_TITLE)), click()))
    }

    /**
     * TODO THIS IS A FLAKY TEST REPLACE ONCE YOU MAKE A MATCHER FOR VIEW-HOLDER
     */
    @Test
    fun openFolderTest() {
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
        onView(withId(R.id.commonList)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_FOLDER_NAME)), click()))
    }

    @After
    fun unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource)
        }
    }

    companion object {

        private val TEST_FOLDER_NAME = "testFolder"
        private val TEMP_NOTE_TITLE = "testNoteTitle"
    }

}
