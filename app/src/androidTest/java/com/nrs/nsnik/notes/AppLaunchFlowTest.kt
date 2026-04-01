package com.nrs.nsnik.notes

import android.widget.FrameLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
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
class AppLaunchFlowTest {

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
    fun mainActivityDisplaysToolbar() {
        onView(withId(R.id.mainToolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun mainActivityDisplaysDrawerLayout() {
        onView(withId(R.id.mainDrawerLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun mainActivityDisplaysNavHost() {
        onView(allOf(withId(R.id.mainNavHost), withParent(withClassName(equalTo("android.widget.FrameLayout")))))
            .check(matches(isDisplayed()))
    }

    @Test
    fun listFragmentDisplaysRecyclerView() {
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
    }

    @Test
    fun listFragmentDisplaysFab() {
        onView(withId(R.id.fabAdd)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
