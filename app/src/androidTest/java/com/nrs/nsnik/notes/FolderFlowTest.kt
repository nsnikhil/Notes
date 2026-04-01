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
class FolderFlowTest {

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
    fun tapAddFolderOpensCreateFolderDialog() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).perform(click())
        onView(withId(R.id.dialogFolderName)).check(matches(isDisplayed()))
    }

    @Test
    fun createFolderDialogHasCreateAndCancelButtons() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).perform(click())
        onView(withId(R.id.dialogFolderCreate)).check(matches(isDisplayed()))
        onView(withId(R.id.dialogFolderCancel)).check(matches(isDisplayed()))
    }

    @Test
    fun enterFolderNameInDialog() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).perform(click())
        onView(withId(R.id.dialogFolderName))
            .perform(replaceText("Test Folder"), closeSoftKeyboard())
        onView(withId(R.id.dialogFolderName)).check(matches(withText("Test Folder")))
    }

    @Test
    fun createFolderDialogHasColorAndLockOptions() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddFolder)).perform(click())
        onView(withId(R.id.dialogFolderColor)).check(matches(isDisplayed()))
        onView(withId(R.id.dialogFolderLock)).check(matches(isDisplayed()))
        onView(withId(R.id.dialogFolderStar)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
