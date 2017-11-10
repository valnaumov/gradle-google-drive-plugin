package ru.kinca.gradle.googledrive

import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider

/**
 * @author Valentin Naumov
 */
class PropertyStateWithDefaultValue<T>
{
    private final Provider<T> defaultValueProvider

    @Delegate
    private final PropertyState<T> delegate

    PropertyStateWithDefaultValue(
        PropertyState<T> delegate,
        T defaultValue)
    {
        this(delegate, { defaultValue } as Provider<T>)
    }

    PropertyStateWithDefaultValue(
        PropertyState<T> delegate,
        Provider<T> defaultValueProvider)
    {
        this.defaultValueProvider = defaultValueProvider
        this.delegate = delegate
    }

    /**
     * If a value is present in this provider, returns the default value.
     *
     * @return value
     * @throws IllegalStateException if there is no value present
     */
    @Override
    T get()
    {
        delegate.present ? delegate.get() : defaultValueProvider.get()
    }

    /**
     * Returns {@code true} if there is a value present, otherwise the default
     * value.
     *
     * @return {@code true} if there is a value present, otherwise the default
     * value.
     */
    @Override
    T getOrNull()
    {
        delegate.present ? delegate.get() : defaultValueProvider.get()
    }
}
