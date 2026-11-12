package com.pedropathing.tuning.autotune.config;

/**
 * Where a variable lives on the dashboard: a category (which config page)
 * and a name (which row).
 */
public class Section {
    public static final String VARIABLES_CATEGORY = "Variables";
    public static final String MODIFIERS_CATEGORY = "Modifiers";

    public String category;
    public String name;
    public boolean isModifier;

    public Section(String category, String name, boolean isModifier) {
        this.category = category;
        this.name = name;
        this.isModifier = isModifier;
    }
}
