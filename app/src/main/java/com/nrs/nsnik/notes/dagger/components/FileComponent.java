package com.nrs.nsnik.notes.dagger.components;

import com.nrs.nsnik.notes.dagger.modules.FileModule;
import com.nrs.nsnik.notes.dagger.scopes.ApplicationScope;
import com.nrs.nsnik.notes.util.FileUtil;

import dagger.Component;

@ApplicationScope
@Component(modules = FileModule.class)
public interface FileComponent {
    FileUtil getFileUtil();
}
