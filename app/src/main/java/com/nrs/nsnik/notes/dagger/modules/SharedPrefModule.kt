package com.nrs.nsnik.notes.dagger.modules

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.nrs.nsnik.notes.dagger.qualifiers.ApplicationQualifier
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope
import dagger.Module
import dagger.Provides

@Module(includes = [(ContextModule::class)])
class SharedPrefModule {

    @ApplicationScope
    @Provides
    fun provideSharedPreferences(@ApplicationQualifier context: Context): SharedPreferences{
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

}