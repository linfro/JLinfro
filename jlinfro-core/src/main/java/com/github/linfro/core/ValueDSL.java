package com.github.linfro.core;

import com.github.linfro.core.common.*;
import com.github.linfro.core.dsl.BothWayLink;
import com.github.linfro.core.dsl.Context;
import com.github.linfro.core.dsl.OneWayLink;
import com.github.linfro.core.value.*;

import static com.github.linfro.core.common.ObjectUtil.notNull;

/**
 * @author Dmitry Ermakov
 * @version 2014-01-06
 * @since 1.0.0
 */
public abstract class ValueDSL<DSL, F, SRC extends GetValue<F>> {
    protected final SRC from;
    protected final Context context;

    private ValueDSL(SRC from, Context context) {
        this.from = notNull(from);
        this.context = notNull(context);
    }

    protected DSL addFromToDispose() {
        context.addToDispose(from);
        return nextDSL();
    }

    protected <A> A addToDispose(A obj) {
        context.addToDispose(obj);
        return obj;
    }

    protected abstract DSL nextDSL();

    protected abstract Disposable createLink(HasValue<F> to);

    //******************************************************************************************************************
    //  Transformation syntax
    //******************************************************************************************************************

    public static final class OneWayDSL<F> extends ValueDSL<OneWayDSL<F>, F, GetValue<F>> {
        private OneWayDSL(GetValue<F> from, Context context) {
            super(from, context);
        }

        @Override
        protected OneWayDSL<F> nextDSL() {
            return this;
        }

        @Override
        protected Disposable createLink(HasValue<F> to) {
            return new OneWayLink<>(from, to, context);
        }

        public <T> OneWayDSL<T> transform(Function<F, T> function) {
            return new OneWayDSL<>(new TransformedGetValue<>(from, function), context).addFromToDispose();
        }
    }

    public static final class BothWayDSL<F> extends ValueDSL<BothWayDSL<F>, F, HasValue<F>> {
        private BothWayDSL(HasValue<F> from, Context context) {
            super(from, context);
        }

        @Override
        protected BothWayDSL<F> nextDSL() {
            return this;
        }

        @Override
        protected Disposable createLink(HasValue<F> to) {
            return new BothWayLink<>(from, to, context);
        }

        public BothWayDSL<F> sync() {
            context.setSync(true);
            return nextDSL();
        }

        public <T> BothWayDSL<T> transform(RevertFunction<F, T> function) {
            return new BothWayDSL<>(new TransformedHasValue<>(from, function), context).addFromToDispose();
        }

        public <T> BothWayDSL<T> transform(Function<F, T> function, Function<T, F> revertFunction) {
            return new BothWayDSL<>(new TransformedHasValue<>(
                    from, new DefaultRevertFunction<>(function, revertFunction)), context
            ).addFromToDispose();
        }
    }

    public static final class HybridDSL<F> extends ValueDSL<HybridDSL<F>, F, HasValue<F>> {
        private HybridDSL(HasValue<F> from, Context context) {
            super(from, context);
        }

        @Override
        protected HybridDSL<F> nextDSL() {
            return this;
        }

        @Override
        protected Disposable createLink(HasValue<F> to) {
            return new BothWayLink<>(from, to, context);
        }

        public <T> HybridDSL<T> transform(RevertFunction<F, T> function) {
            return new HybridDSL<>(new TransformedHasValue<>(from, function), context).addFromToDispose();
        }

        public <T> HybridDSL<T> transform(Function<F, T> function, Function<T, F> revertFunction) {
            return new HybridDSL<>(new TransformedHasValue<>(
                    from, new DefaultRevertFunction<>(function, revertFunction)), context
            ).addFromToDispose();
        }

        // One way branch
        public <T> OneWayDSL<T> transform(Function<F, T> function) {
            return new OneWayDSL<>(new TransformedGetValue<>(from, function), context).addFromToDispose();
        }

        // Both way branch
        public BothWayDSL<F> sync() {
            context.setSync(true);
            return new BothWayDSL<>(from, context);
        }
    }

    //******************************************************************************************************************
    //  Common syntax
    //******************************************************************************************************************

    public static <A> DefaultHasValue<A> newHasValue() {
        return new DefaultHasValue<>();
    }

    public static <A> DefaultHasValue<A> newHasValue(A value) {
        return new DefaultHasValue<>(value);
    }

    public static <F> OneWayDSL<F> linkFrom(GetValue<F> from) {
        return new OneWayDSL<>(from, new Context());
    }

    public static <F> HybridDSL<F> linkFrom(HasValue<F> from) {
        return new HybridDSL<>(from, new Context());
    }

    public DSL strong() {
        context.setStrong(true);
        return nextDSL();
    }

    public DSL strong(Equality equality) {
        context.setStrong(true);
        context.setEquality(equality);
        return nextDSL();
    }

    public DSL force() {
        context.setForce(true);
        return nextDSL();
    }

    public Disposable to(HasValue<F> to) {
        return createLink(to);
    }

    //******************************************************************************************************************
    //  Aggregation syntax
    //******************************************************************************************************************

    public Disposable to(HasAggregateValue<F> aggregateValue) {
        notNull(aggregateValue);
        return to(addToDispose(notNull(aggregateValue.newMemberValue())));
    }

    public static <A> DefaultHasAggregateValue<A> newAggregateValue(Aggregator<A> aggregator) {
        notNull(aggregator);
        return new DefaultHasAggregateValue<>(aggregator);
    }

    public static DefaultHasAggregateValue<Boolean> andValue() {
        return newAggregateValue(ValueUtil.AGGREGATOR_AND);
    }

    public static DefaultHasAggregateValue<Boolean> orValue() {
        return newAggregateValue(ValueUtil.AGGREGATOR_OR);
    }

    public static RevertFunction<Boolean, Boolean> not() {
        return ValueUtil.NOT_FUNCTION;
    }
}
