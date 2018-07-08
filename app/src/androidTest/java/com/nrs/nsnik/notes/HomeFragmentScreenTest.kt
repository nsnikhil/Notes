/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

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
