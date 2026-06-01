package com.ansimue.kajimbatsiko;

import android.app.Application;
import com.ansimue.kajimbatsiko.utils.ThemeManager;

public class KajimbatsikoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Appliquer le thème sauvegardé avant tout affichage
        ThemeManager.applyTheme(this);
    }
}
