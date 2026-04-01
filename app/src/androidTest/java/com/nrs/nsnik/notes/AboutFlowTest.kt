package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
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
class AboutFlowTest {

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
    fun navigateToAboutShowsAuthorInfo() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemAbout))
        onView(withId(R.id.aboutNikhil)).check(matches(isDisplayed()))
    }

    @Test
    fun aboutShowsLibrariesButton() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemAbout))
        onView(withId(R.id.aboutLibraries)).check(matches(isDisplayed()))
    }

    @Test
    fun aboutShowsLicenseButton() {
        onView(withId(R.id.mainDrawerLayout)).perform(DrawerActions.open())
        onView(withId(R.id.mainNavigationView))
            .perform(NavigationViewActions.navigateTo(R.id.navItemAbout))
        onView(withId(R.id.aboutLicense)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
