package com.github.linfro.core;

import com.github.linfro.core.common.Disposable;
import com.github.linfro.core.common.Equality;

import java.util.function.Consumer;

/**
 * @author Dmitry Ermakov
 * @version 2014-02-23
 * @since 1.0.0
 */
public interface GetValueFlow<F> {
    public GetValueFlow<F> strong();

    public GetValueFlow<F> strong(Equality equality);

    public GetValueFlow<F> force();

    public Disposable to(HasValueHolder<F> to);

    public Disposable to(Consumer<? super F> consumer);

    public Disposable to(GetAggregateValue<F> aggregateValue);
}