/*
 * MIT License
 *
 * Copyright (c) 2018 Andavin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andavin.reflect;

import com.andavin.reflect.exception.UncheckedNoSuchFieldException;
import com.andavin.reflect.exception.UncheckedReflectiveOperationException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @since November 03, 2018
 * @author Andavin
 */
public class FieldMatcher extends AttributeMatcher<Field, FieldMatcher> {

    public FieldMatcher(Class<?> type) {
        super(type, Modifier.fieldModifiers());
    }

    @Override
    boolean match(Field field) {
        return this.match(field.getModifiers(), field.getType());
    }

    @Override
    UncheckedReflectiveOperationException buildException() {
        return new UncheckedNoSuchFieldException("Could not find field with type " + this.mainType.getSimpleName() +
                " requiring " + Integer.toBinaryString(this.requiredModifiers) + " and disallowing " +
                Integer.toBinaryString(this.disallowedModifiers));
    }
}
