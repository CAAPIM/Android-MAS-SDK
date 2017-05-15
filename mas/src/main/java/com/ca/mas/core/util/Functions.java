/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.util;

/**
 * Holds simple callback interfaces.
 */
public class Functions {

    /**
     * A function that takes one argument and returns nothing.
     *
     * @param <A> parameter type
     */
    public interface UnaryVoid<A> {
        void call(A a);
    }

    public interface Nullary<R> {
        R call();
    }

    public interface NullaryVoid {
        void call();
    }

    public interface Unary<R, A> {
        R call(A a);
    }

    public interface BinaryVoid<A, B> {
        void call(A a, B b);
    }

    public interface Binary<R, A, B> {
        R call(A a, B b);
    }

    private Functions() {}
}
