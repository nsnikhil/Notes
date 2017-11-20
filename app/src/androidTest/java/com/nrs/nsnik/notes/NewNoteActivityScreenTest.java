package com.nrs.nsnik.notes;


import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.nrs.nsnik.notes.view.NewNoteActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewNoteActivityScreenTest {

    private static final String TEMP_NOTE_TITLE = "testNoteTitle";
    private static final String TEMP_NOTE_BODY = "testNoteBody";
    private static final String TEMP_CHECK_LIST_ITEM = "test";
    @Rule
    public ActivityTestRule<NewNoteActivity> mActivityTestRule = new ActivityTestRule<>(NewNoteActivity.class);
    private IdlingResource mIdlingResource;

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    /**
     * TODO FIND A WAY TO CLICK ON ITEM BY NOT APPLYING THREAD SLEP
     */
    @Test
    public void openBottomSheetTest() {
        //onView(withId(R.id.toolsDate)).perform(click());
        //onView(withId(R.id.toolsColor)).check(matches(isDisplayed()));
    }

    /**
     * TODO FIND A WAY TO CLICK ON ITEM BY NOT APPLYING THREAD SLEP
     */
    @Test
    public void addCheckList() throws InterruptedException {
        //onView(withId(R.id.toolsDate)).perform(click());
        //onView(withId(R.id.toolsCheckList)).check(matches(isDisplayed()));
        //onView(withId(R.id.toolsCheckList)).perform(click());
        //onView(withId(R.id.checkListItem)).check(matches(isDisplayed()));
        //onView(withId(R.id.checkListItem)).perform(typeText(TEMP_CHECK_LIST_ITEM));
        //onView(withId(R.id.checkListTicker)).perform(click());
        //onView(withId(R.id.newNoteCheckList)).check(matches(hasDescendant(withText(TEMP_CHECK_LIST_ITEM))));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

}
