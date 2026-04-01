package com.nrs.nsnik.notes

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
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
class ImageAttachmentFlowTest {

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
        Intents.init()
    }

    private fun navigateToNewNote() {
        onView(withId(R.id.fabAdd)).perform(click())
        onView(withId(R.id.fabAddNote)).perform(click())
        onView(withId(R.id.newNoteTitle))
            .perform(replaceText("Image Test"), closeSoftKeyboard())
    }

    @Test
    fun cameraToolIsDisplayedInBottomSheet() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsCamera)).check(matches(isDisplayed()))
    }

    @Test
    fun galleryToolIsDisplayedInBottomSheet() {
        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsAttachment)).check(matches(isDisplayed()))
    }

    @Test
    fun tapCameraToolWithStubbedIntent() {
        val resultData = Intent()
        val result = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData)
        Intents.intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)

        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsCamera)).perform(click())
    }

    @Test
    fun tapGalleryToolWithStubbedIntent() {
        val resultData = Intent()
        val result = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData)
        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)

        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsAttachment)).perform(click())
    }

    @Test
    fun testCameraIntentCancelled() {
        // Stub camera to return RESULT_CANCELED — no image should be added
        val result = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, Intent())
        Intents.intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result)

        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsCamera)).perform(click())
        Thread.sleep(600)

        // Image list should remain hidden (GONE) since no image was captured
        onView(withId(R.id.newNoteImageList))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testGalleryIntentCancelled() {
        // Stub gallery to return RESULT_CANCELED — no image should be added
        val result = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, Intent())
        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)

        navigateToNewNote()
        onView(withId(R.id.toolsDate)).perform(click())
        onView(withId(R.id.toolsAttachment)).perform(click())
        Thread.sleep(600)

        // Image list should remain hidden since no image was selected
        onView(withId(R.id.newNoteImageList))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testCameraAndGalleryBothWork() {
        // Stub both camera and gallery with RESULT_CANCELED
        val cancelResult = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, Intent())
        Intents.intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(cancelResult)
        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(cancelResult)

        navigateToNewNote()

        // Tap camera — changeState() expands then the click handler collapses the sheet
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(400)
        onView(withId(R.id.toolsCamera)).perform(click())
        Thread.sleep(800)

        // Re-expand bottom sheet and tap gallery
        onView(withId(R.id.toolsDate)).perform(click())
        Thread.sleep(400)
        onView(withId(R.id.toolsAttachment)).perform(click())
        Thread.sleep(800)

        // Verify both intents were fired
        Intents.intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
        Intents.intended(hasAction(Intent.ACTION_GET_CONTENT))
    }

    @After
    fun tearDown() {
        Intents.release()
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }
}
