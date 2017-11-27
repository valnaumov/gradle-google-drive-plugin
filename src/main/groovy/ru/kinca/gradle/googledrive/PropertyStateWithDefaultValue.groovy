package ru.kinca.gradle.googledrive

import org.gradle.api.internal.provider.DefaultPropertyState
import org.gradle.api.provider.PropertyState

/**
 * Property state that is instantiated with a default value. If no value was set
 * explicitly, the default one is used. The default value may be null.
 *
 * @author Valentin Naumov
 */
class PropertyStateWithDefaultValue<T>
{
    private static final NULL_PROVIDER = { null }

    private final Closure<T> defaultValueProvider

    @Delegate
    private final PropertyState<T> delegate

    /**
     * Creates the property state with default value of {@code null}.
     */
    PropertyStateWithDefaultValue()
    {
        this(NULL_PROVIDER)
    }

    PropertyStateWithDefaultValue(
        T defaultValue)
    {
        this({ defaultValue })
    }

    PropertyStateWithDefaultValue(
        Closure<T> defaultValueProvider)
    {
        this.defaultValueProvider = defaultValueProvider
        this.delegate = new DefaultPropertyState()
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
        delegate.present ? delegate.get() : defaultValueProvider.call()
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
        delegate.present ? delegate.get() : defaultValueProvider.call()
    }
}
