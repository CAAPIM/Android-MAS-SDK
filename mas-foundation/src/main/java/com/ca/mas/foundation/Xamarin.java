/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicate this class or method is required for Xarmarin binding
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Xamarin {

    /** Defines the reason why we need this for binding */
    String value() default "";

}
