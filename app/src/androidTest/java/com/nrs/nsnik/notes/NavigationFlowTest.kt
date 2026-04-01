package com.nrs.nsnik.notes

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.NavigationViewActions
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
class NavigationFlowTest {

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
    fun drawerOpensAndCloses() {
        onView(withId(R.id.mainDrawerLayout))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())
        onView(withId(R.id.mainDrawerLayout))
            .check(matches(isOpen(Gravity.START)))
        onView(withId(R.id.mainDrawerLayout))
            .perform(DrawerActions.close())
        onView(withId(R.id.mainDrawerLayout))
            .check(matches(isClosed(Gravity.START)))
    }

    @Test
    fun drawerShowsNavigationItems() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToSettingsFragment() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemSettings))
        // Toolbar should still be visible after navigating to Settings
        onView(withId(R.id.mainToolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToAboutFragment() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemAbout))
        onView(withId(R.id.aboutNikhil)).check(matches(isDisplayed()))
    }

    @Test
    fun backFromAboutReturnsToNotesList() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemAbout))
        onView(withId(R.id.aboutNikhil)).check(matches(isDisplayed()))
        pressBack()
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
    }

    @Test
    fun backFromSettingsReturnsToNotesList() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemSettings))
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
