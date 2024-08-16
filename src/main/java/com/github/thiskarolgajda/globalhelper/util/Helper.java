package com.github.thiskarolgajda.globalhelper.util;

import com.github.thiskarolgajda.globalhelper.debug.PluginDebugger;
import com.github.thiskarolgajda.globalhelper.injection.DependencyInjection;

public class Helper {

    public void debug(String message) {
        DependencyInjection.get(PluginDebugger.class).debug(message);
    }
}
