package com.nrs.nsnik.notes.util

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecurityChecksTest {

    private lateinit var securityChecks: SecurityChecks

    @Before
    fun setUp() {
        securityChecks = SecurityChecks(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun checkAppSignature_doesNotCrash() {
        // Robolectric signature won't match the hardcoded constant
        val result = securityChecks.checkAppSignature()
        assertFalse(result)
    }

    @Test
    fun verifyInstaller_returnsFalseUnderRobolectric() {
        // Robolectric has no installer package
        val result = securityChecks.verifyInstaller()
        assertFalse(result)
    }

    @Test
    fun checkEmulator_doesNotCrash() {
        // Just verify it runs without exception
        securityChecks.checkEmulator()
    }

    @Test
    fun checkDebuggable_returnsBoolean() {
        val result = securityChecks.checkDebuggable()
        // Robolectric debug app should be debuggable
        assertTrue(result)
    }
}
