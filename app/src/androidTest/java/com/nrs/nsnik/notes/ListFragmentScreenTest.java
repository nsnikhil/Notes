package com.nrs.nsnik.notes;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.nrs.nsnik.notes.view.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ListFragmentScreenTest {

    private static final String TEST_FOLDER_NAME = "testFolder";
    private static final String TEMP_NOTE_TITLE = "testNoteTitle";
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
    private IdlingResource mIdlingResource;

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    /**
     * TODO THIS IS A FLAKY TEST REPLACE ONCE YOU MAKE A MATCHER FOR VIEW-HOLDER
     */
    @Test
    public void openNotesActivityTest() {
        onView(withId(R.id.commonList)).check(matches(isDisplayed()));
        onView(withId(R.id.commonList)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEMP_NOTE_TITLE)), click()));
    }

    /**
     * TODO THIS IS A FLAKY TEST REPLACE ONCE YOU MAKE A MATCHER FOR VIEW-HOLDER
     */
    @Test
    public void openFolderTest() {
        onView(withId(R.id.commonList)).check(matches(isDisplayed()));
        onView(withId(R.id.commonList)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_FOLDER_NAME)), click()));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

}