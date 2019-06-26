/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.controller;

import io.github.vocabhunter.gui.i18n.I18nManager;
import io.github.vocabhunter.gui.i18n.SupportedLocale;
import io.github.vocabhunter.gui.model.MainModel;
import io.github.vocabhunter.gui.settings.SettingsManager;
import io.github.vocabhunter.gui.status.StatusManager;
import javafx.scene.control.Button;
import javafx.scene.control.RadioMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LanguageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LanguageHandler.class);

    private final MainModel mainModel;

    private final GuiFileHandler guiFileHandler;

    private final StatusManager statusManager;

    private final I18nManager i18nManager;

    private final SettingsManager settingsManager;

    private final Map<SupportedLocale, RadioMenuItem> itemMap = new EnumMap<>(SupportedLocale.class);

    private Runnable localeChangeConsumer;

    @Inject
    public LanguageHandler(final MainModel mainModel, final GuiFileHandler guiFileHandler, final StatusManager statusManager, final I18nManager i18nManager, final SettingsManager settingsManager) {
        this.mainModel = mainModel;
        this.guiFileHandler = guiFileHandler;
        this.statusManager = statusManager;
        this.i18nManager = i18nManager;
        this.settingsManager = settingsManager;
    }

    public void initialise() {
        SupportedLocale locale = settingsManager.getLocale()
            .orElse(null);

        if (locale == null) {
            locale = SupportedLocale.DEFAULT_LOCALE;
            settingsManager.setLocale(locale);
        }

        i18nManager.setupLocale(locale);
        mainModel.setLocale(locale);
    }

    public void initialiseLocaleChangeConsumer(final Runnable localeChangeConsumer) {
        this.localeChangeConsumer = localeChangeConsumer;
    }

    public void setupControls(final RadioMenuItem menuEnglish, final RadioMenuItem menuSpanish) {
        setupLocale(menuEnglish, SupportedLocale.ENGLISH);
        setupLocale(menuSpanish, SupportedLocale.SPANISH);
        updateSelection();
    }

    public void setupLanguageSelectionControl(final SupportedLocale locale, final Button button) {
        button.setOnAction(e -> localeSelectionAction(locale));
    }

    private void setupLocale(final RadioMenuItem menuItem, final SupportedLocale locale) {
        itemMap.put(locale, menuItem);
        menuItem.setOnAction(e -> localeSelectionAction(locale));
    }

    private void localeSelectionAction(final SupportedLocale locale) {
        if (mainModel.getLocale() != locale && guiFileHandler.unsavedChangesCheck()) {
            settingsManager.setLocale(locale);
            i18nManager.setupLocale(locale);
            mainModel.setLocale(locale);
            mainModel.clearSessionModel();
            statusManager.clearSession();
            localeChangeConsumer.run();
            LOG.info("Language changed to {}", locale);
        }
        updateSelection();
    }

    private void updateSelection() {
        SupportedLocale locale = mainModel.getLocale();

        itemMap.forEach((l, i) -> i.setSelected(l == locale));
    }
}
