package com.nrs.nsnik.notes

import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.preference.PreferenceManager
import com.nrs.nsnik.notes.view.MainActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that pressing Enter (IME_ACTION_NEXT) in a checklist item
 * creates exactly one new item each time, not duplicates.
 */
@RunWith(AndroidJUnit4::class)
class ChecklistEnterKeyTest {

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<MainActivity> =
        ActivityTestRule(MainActivity::class.java, false, false)

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
            .perform(replaceText("Enter Key Test"), closeSoftKeyboard())
    }

    private fun expandBottomSheetAndTapChecklist() {
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(600)
        onView(withId(R.id.toolsCheckList)).perform(click())
        Thread.sleep(600)
    }

    @Test
    fun enterKeyCreatesExactlyOneNewItemEachTime() {
        navigateToNewNote()
        expandBottomSheetAndTapChecklist()

        // Starts with 1 item
        onView(withId(R.id.newNoteCheckList)).check(matches(recyclerViewItemCount(1)))

        // Type text in item 0 and press Enter
        onView(withId(R.id.newNoteCheckList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, typeInChecklistItem("Item one")
            )
        )
        Thread.sleep(300)
        onView(withId(R.id.newNoteCheckList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, pressImeOnChecklistItem()
            )
        )
        Thread.sleep(800)

        // Exactly 2 items after first Enter
        onView(withId(R.id.newNoteCheckList)).check(matches(recyclerViewItemCount(2)))

        // Type text in item 1 and press Enter again
        onView(withId(R.id.newNoteCheckList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, typeInChecklistItem("Item two")
            )
        )
        Thread.sleep(300)
        onView(withId(R.id.newNoteCheckList)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, pressImeOnChecklistItem()
            )
        )
        Thread.sleep(800)

        // Exactly 3 items after second Enter
        onView(withId(R.id.newNoteCheckList)).check(matches(recyclerViewItemCount(3)))
    }

    private fun typeInChecklistItem(text: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "type '$text' in checklist EditText"
            override fun perform(uiController: UiController, view: View) {
                val editText = view.findViewById<EditText>(R.id.checkListItem)
                editText.setText(text)
                uiController.loopMainThreadUntilIdle()
            }
        }
    }

    private fun pressImeOnChecklistItem(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "press IME action on checklist EditText"
            override fun perform(uiController: UiController, view: View) {
                val editText = view.findViewById<EditText>(R.id.checkListItem)
                editText.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT)
                uiController.loopMainThreadUntilIdle()
            }
        }
    }

    private fun recyclerViewItemCount(expectedCount: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("RecyclerView with item count: $expectedCount")
        }

        override fun matchesSafely(item: View): Boolean {
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
