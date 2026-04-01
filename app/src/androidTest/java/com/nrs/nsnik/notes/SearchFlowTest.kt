package com.nrs.nsnik.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.appcompat.widget.SearchView
import com.nrs.nsnik.notes.view.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchFlowTest {

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
    fun searchIconIsDisplayed() {
        onView(withId(R.id.menuMainSearch)).check(matches(isDisplayed()))
    }

    @Test
    fun tapSearchExpandsSearchView() {
        onView(withId(R.id.menuMainSearch)).perform(click())
        onView(isAssignableFrom(SearchView::class.java)).check(matches(isDisplayed()))
    }

    @Test
    fun typeQueryInSearchView() {
        onView(withId(R.id.menuMainSearch)).perform(click())
        onView(isAssignableFrom(SearchView.SearchAutoComplete::class.java))
            .perform(typeText("test query"), closeSoftKeyboard())
        // RecyclerView should still be displayed (showing filtered results or empty)
        onView(withId(R.id.commonList)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
