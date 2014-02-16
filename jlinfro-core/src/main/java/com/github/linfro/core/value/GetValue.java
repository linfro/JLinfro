package com.github.linfro.core.value;

import com.github.linfro.core.Flow;
import com.github.linfro.core.IOutFlow;
import com.github.linfro.core.common.AutoDisposable;
import com.github.linfro.core.common.MetaInfoHolder;
import com.github.linfro.core.common.NullSafeFunction;
import com.github.linfro.core.common.NvlFunction;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Dmitry Ermakov
 * @version 2014-01-04
 * @since 1.0.0
 */
public interface GetValue<T> extends Getter<T> {
    @Override
    public T getValue();

    public void addChangeListener(ValueChangeListener<? super T> listener);

    public void removeChangeListener(ValueChangeListener<? super T> listener);

    public default void autoDispose() {
        if (this instanceof AutoDisposable) {
            AutoDisposable disposable = (AutoDisposable) this;
            if (disposable.isAutoDispose()) {
                disposable.dispose();
            }
        }
    }

    public default IOutFlow<T> flow() {
        return Flow.from(this);
    }

    public default <M> GetDisposableValue<M> map(Function<T, M> function) {
        return new GetTransformedValue<>(this, function);
    }

    public default <M> GetDisposableValue<M> mapNotNull(Function<T, M> function) {
        return new GetTransformedValue<>(this, new NullSafeFunction<>(function));
    }

    public default GetDisposableValue<T> nvl(T nullValue) {
        return new GetTransformedValue<>(this, new NvlFunction<T>(nullValue));
    }

    public default GetDisposableValue<T> filter(Predicate<? super T> predicate) {
        return new GetFilteredValue<>(this, predicate);
    }

    // Meta info support

    public default Object findMetaInfo(String key) {
        return this instanceof MetaInfoHolder ? ((MetaInfoHolder) this).getMetaInfo(key) : null;
    }

    public default GetDisposableValue<T> putMetaInfo(String metaInfoKey, Object metaInfoValue) {
        return new GetMetaInfoValue<>(this, metaInfoKey, metaInfoValue);
    }

    public default String findMetaName() {
        return (String) findMetaInfo(MetaInfoHolder.META_NAME);
    }

    public default GetDisposableValue<T> named(String name) {
        return putMetaInfo(MetaInfoHolder.META_NAME, name);
    }

    @SuppressWarnings("unchecked")
    public default GetDisposableValue<List<T>> union(GetValue<? extends T>... args) {
        return new GetUnionValue<>(this, args);
    }
}
