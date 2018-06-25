package com.nrs.nsnik.notes

import android.view.Gravity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.nrs.nsnik.notes.view.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutFragmentScreenTest {

    @Rule
    var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    private var mIdlingResource: IdlingResource? = null

    @Before
    fun registerIdlingResource() {
        mIdlingResource = mActivityTestRule.activity.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }


    fun openAboutFragment() {
        onView(withId(R.id.mainDrawerLayout))
                .check(matches(isClosed(Gravity.START)))
                .perform(DrawerActions.open())

        onView(withId(R.id.mainNavigationView))
                .perform(NavigationViewActions.navigateTo(R.id.navItem5))

        onView(withId(R.id.aboutNikhil)).check(matches(isDisplayed()))
    }

    @Test
    fun openLibrariesListTest() {
        openAboutFragment()
        onView(withId(R.id.aboutLibraries)).check(matches(isDisplayed()))
        onView(withId(R.id.aboutLibraries)).perform(click())
    }

    @Test
    fun openLicenseTest() {
        openAboutFragment()
        onView(withId(R.id.aboutLicense)).check(matches(isDisplayed()))
        onView(withId(R.id.aboutLicense)).perform(click())
    }

    @After
    fun unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource)
        }
    }

}
