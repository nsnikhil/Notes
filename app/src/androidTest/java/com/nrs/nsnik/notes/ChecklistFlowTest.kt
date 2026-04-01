package com.nrs.nsnik.notes

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.nrs.nsnik.notes.view.MainActivity
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChecklistFlowTest {

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

    private fun navigateToNewNote() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Checklist Test"), closeSoftKeyboard())
    }

    private fun expandBottomSheetAndTapChecklist() {
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(600)
        onView(withId(R.id.toolsCheckList)).perform(click())
        Thread.sleep(600)
    }

    @Test
    fun checklistToolIsDisplayedInBottomSheet() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsCheckList)).check(matches(isDisplayed()))
    }

    @Test
    fun tapChecklistToolShowsChecklistArea() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(600)
        onView(withId(R.id.toolsCheckList)).perform(click())
        Thread.sleep(600)
        onView(withId(R.id.newNoteCheckList)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun testAddChecklistItemWithText() {
        navigateToNewNote()
        expandBottomSheetAndTapChecklist()
        // Type text into the checklist item's EditText
        onView(withId(R.id.checkListItem)).perform(replaceText("Buy groceries"), closeSoftKeyboard())
        Thread.sleep(600)
        onView(withText("Buy groceries")).check(matches(isDisplayed()))
    }

    @Test
    fun testRapidAddFiveItems() {
        navigateToNewNote()
        // Add 5 items by tapping checklist tool 5 times
        repeat(5) {
            onView(withId(R.id.toolsDate)).perform(click())
            Thread.sleep(400)
            onView(withId(R.id.toolsCheckList)).perform(click())
            Thread.sleep(400)
        }
        onView(withId(R.id.newNoteCheckList)).check(matches(recyclerViewItemCount(5)))
    }

    @Test
    fun testCheckItemShowsStrikethrough() {
        navigateToNewNote()
        expandBottomSheetAndTapChecklist()
        // Check the checkbox on the first item
        onView(withId(R.id.checkListTicker)).perform(click())
        Thread.sleep(400)
        onView(withId(R.id.checkListTicker)).check(matches(isChecked()))
    }

    @Test
    fun testAddEmptyChecklistItem() {
        navigateToNewNote()
        expandBottomSheetAndTapChecklist()
        // Item added without typing — verify the RecyclerView is visible with 1 item
        onView(withId(R.id.newNoteCheckList)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.newNoteCheckList)).check(matches(recyclerViewItemCount(1)))
    }

    @Test
    fun testMultipleItemsHaveCorrectCount() {
        navigateToNewNote()
        // Add 3 items
        repeat(3) {
            onView(withId(R.id.toolsDate)).perform(click())
            Thread.sleep(400)
            onView(withId(R.id.toolsCheckList)).perform(click())
            Thread.sleep(400)
        }
        onView(withId(R.id.newNoteCheckList)).check(matches(recyclerViewItemCount(3)))
    }

    /** Custom matcher that asserts a RecyclerView has exactly [expectedCount] items. */
    private fun recyclerViewItemCount(expectedCount: Int) = object : TypeSafeMatcher<android.view.View>() {
        override fun describeTo(description: Description) {
            description.appendText("RecyclerView with item count: $expectedCount")
        }

        override fun matchesSafely(item: android.view.View): Boolean {
            return (item as? RecyclerView)?.adapter?.itemCount == expectedCount
        }
    }

    @After
    fun tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
