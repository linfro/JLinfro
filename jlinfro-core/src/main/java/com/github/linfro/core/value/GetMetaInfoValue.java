package com.github.linfro.core.value;

import com.github.linfro.core.common.AutoDisposable;
import com.github.linfro.core.common.MetaInfoHolder;

import static com.github.linfro.core.common.ObjectUtil.notNull;

/**
 * @author Dmitry Ermakov
 * @version 2014-02-13
 * @since 1.0.0
 */
public class GetMetaInfoValue<T> extends AbstractGetWrapperValue<T, T>
        implements GetDisposableValue<T>, AutoDisposable, MetaInfoHolder {
    protected final String metaInfoKey;
    protected final Object metaInfoValue;

    public GetMetaInfoValue(GetValue<T> from, String metaInfoKey, Object metaInfoValue) {
        super(from);
        this.metaInfoKey = notNull(metaInfoKey);
        this.metaInfoValue = metaInfoValue;
    }

    @Override
    public T getValue() {
        if (disposed) {
            throw new IllegalStateException("Value is disposed");
        }

        return from.getValue();
    }

    @Override
    public Object getMetaInfo(String key) {
        return metaInfoKey.equals(key) ? metaInfoValue : super.getMetaInfo(key);
    }
}
