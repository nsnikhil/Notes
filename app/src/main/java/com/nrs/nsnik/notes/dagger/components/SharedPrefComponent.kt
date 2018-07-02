package com.nrs.nsnik.notes.dagger.components

import android.content.SharedPreferences
import com.nrs.nsnik.notes.dagger.modules.SharedPrefModule
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import dagger.Component

@ApplicationScope
@Component(modules = [(SharedPrefModule::class)])
interface SharedPrefComponent{
    val sharedPreferences: SharedPreferences
}