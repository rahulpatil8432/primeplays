package com.rkonline.android.model;

import java.util.ArrayList;

public class GameHandler {
    public interface NumberGenerator {
        ArrayList<String> generate();
    }

    final NumberGenerator generator;
    public final Class<?> targetActivity;
    public final int icon;

    public GameHandler(int icon,NumberGenerator generator, Class<?> targetActivity) {
        this.icon = icon;
        this.generator = generator;
        this.targetActivity = targetActivity;
    }

    public ArrayList<String> generateNumbers() {
        return generator.generate();
    }
}