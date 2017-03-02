package com.nrs.nsnik.notes;

import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class Prefrences extends AppCompatActivity {

    Toolbar prefrenceToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefrences);
        initilize();
        getFragmentManager().beginTransaction().add(R.id.prefrenceContainer,new PrefrenceFragment()).commit();
    }

    private void initilize() {
        prefrenceToolbar = (Toolbar) findViewById(R.id.prefrenceToolbar);
        prefrenceToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(prefrenceToolbar);
    }

    public static class PrefrenceFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_prefs);
        }
    }
}
