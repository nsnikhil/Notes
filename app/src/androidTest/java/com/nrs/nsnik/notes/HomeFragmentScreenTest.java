package com.nrs.nsnik.notes;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
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
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class HomeFragmentScreenTest {

    private static final String TEST_FOLDER_NAME = "testFolder";
    private static final String TEMP_NOTE_TITLE = "testNoteTitle";
    private static final String TEMP_NOTE_BODY = "testNoteBody";
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
    private IdlingResource mIdlingResource;

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void openFabTest() {
        onView(withId(R.id.fabAdd)).perform(click());
    }

    @Test
    public void openAddNotesActivityTest() {
        onView(withId(R.id.fabAdd)).perform(click());
        onView(withId(R.id.fabAddNote)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddNote)).perform(click());
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void openAddFolderDialogTest() {
        onView(withId(R.id.fabAdd)).perform(click());
        onView(withId(R.id.fabAddFolder)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddFolder)).perform(click());
        onView(withId(R.id.dialogFolderName)).check(matches(isDisplayed()));
    }

    @Test
    public void addNewFolderTest() {
        onView(withId(R.id.fabAdd)).perform(click());
        onView(withId(R.id.fabAddFolder)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddFolder)).perform(click());
        onView(withId(R.id.dialogFolderName)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogFolderName)).perform(typeText(TEST_FOLDER_NAME));
        onView(withId(R.id.dialogFolderCreate)).perform(click());
        onView(withId(R.id.commonList)).check(matches(isDisplayed()));
        onView(withId(R.id.commonList)).check(matches(hasDescendant(withText(TEST_FOLDER_NAME))));
    }

    @Test
    public void addNewNoteTest() {
        onView(withId(R.id.fabAdd)).perform(click());
        onView(withId(R.id.fabAddNote)).check(matches(isDisplayed()));
        onView(withId(R.id.fabAddNote)).perform(click());
        onView(withId(R.id.newNoteTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.newNoteTitle)).perform(typeText(TEMP_NOTE_TITLE));
        onView(withId(R.id.newNoteContent)).perform(typeText(TEMP_NOTE_BODY), closeSoftKeyboard());
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.commonList)).check(matches(isDisplayed()));
        onView(withId(R.id.commonList)).check(matches(hasDescendant(withText(TEMP_NOTE_TITLE))));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

}
