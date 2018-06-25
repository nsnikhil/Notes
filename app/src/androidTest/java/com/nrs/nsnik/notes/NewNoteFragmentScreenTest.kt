package com.nrs.nsnik.notes


import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

import com.nrs.nsnik.notes.view.fragments.NewNoteFragment

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NewNoteFragmentScreenTest {
    @Rule
    var mActivityTestRule: ActivityTestRule<NewNoteFragment> = ActivityTestRule(NewNoteFragment::class.java)
    private var mIdlingResource: IdlingResource? = null

    @Before
    fun registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    /**
     * TODO FIND A WAY TO CLICK ON ITEM BY NOT APPLYING THREAD SLEP
     */
    @Test
    fun openBottomSheetTest() {
        //onView(withId(R.id.toolsDate)).perform(click());
        //onView(withId(R.id.toolsColor)).check(matches(isDisplayed()));
    }

    /**
     * TODO FIND A WAY TO CLICK ON ITEM BY NOT APPLYING THREAD SLEP
     */
    @Test
    @Throws(InterruptedException::class)
    fun addCheckList() {
        //onView(withId(R.id.toolsDate)).perform(click());
        //onView(withId(R.id.toolsCheckList)).check(matches(isDisplayed()));
        //onView(withId(R.id.toolsCheckList)).perform(click());
        //onView(withId(R.id.checkListItem)).check(matches(isDisplayed()));
        //onView(withId(R.id.checkListItem)).perform(typeText(TEMP_CHECK_LIST_ITEM));
        //onView(withId(R.id.checkListTicker)).perform(click());
        //onView(withId(R.id.newNoteCheckList)).check(matches(hasDescendant(withText(TEMP_CHECK_LIST_ITEM))));
    }

    @After
    fun unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource)
        }
    }

    companion object {

        private val TEMP_NOTE_TITLE = "testNoteTitle"
        private val TEMP_NOTE_BODY = "testNoteBody"
        private val TEMP_CHECK_LIST_ITEM = "test"
    }

}
