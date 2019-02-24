/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.analysis.settings;

import io.github.vocabhunter.analysis.core.FileTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class BaseSettingsManager<T> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseSettingsManager.class);

    private final Path settingsFile;

    private final Class<T> beanClass;

    private final AtomicReference<T> settingsReference = new AtomicReference<>();

    protected BaseSettingsManager(final String filename, final Class<T> beanClass) {
        this(SettingsPathTool.obtainSettingsFilePath(filename), beanClass);
    }

    protected BaseSettingsManager(final Path settingsFile, final Class<T> beanClass) {
        this.settingsFile = settingsFile;
        this.beanClass = beanClass;
        FileTool.ensureDirectoryExists(settingsFile, "Unable to create directory %s");
    }

    public void initialise() {
        readSettings();
    }

    protected T readSettings() {
        T result = readSettingsIfAvailable();

        if (result == null) {
            FileTool.writeMinimalJson(settingsFile, "Unable to save settings file '%s'");
            result = readSettingsIfAvailable();
        }

        return result;
    }

    private T readSettingsIfAvailable() {
        T result = settingsReference.get();

        try {
            if (result == null && Files.isRegularFile(settingsFile)) {
                result = FileTool.readFromJson(beanClass, settingsFile, "Unable to load settings file '%s'");
                settingsReference.set(result);
            }
        } catch (final Exception e) {
            LOG.error("Discarding unreadable settings file '{}'", settingsFile, e);
        }

        return result;
    }

    protected void writeSettings(final T settings) {
        FileTool.writeAsJson(settingsFile, settings, "Unable to save settings file '%s'");
        settingsReference.set(settings);
    }
}
