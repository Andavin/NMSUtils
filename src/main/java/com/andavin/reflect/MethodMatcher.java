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

import com.andavin.util.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.andavin.reflect.Reflection.compare;

/**
 * @since November 03, 2018
 * @author Andavin
 */
public class MethodMatcher extends AttributeMatcher<Method, MethodMatcher> {

    private static final int BRIDGE = 0x40;
    private final Class<?>[] parametersTypes;

    public MethodMatcher(Class<?> returnType, Class<?>... parametersTypes) {
        super(returnType, Modifier.methodModifiers() | BRIDGE);
        this.parametersTypes = parametersTypes;
    }

    public MethodMatcher requireBridge() {

        this.requiredModifiers |= BRIDGE;
        if ((this.disallowedModifiers & BRIDGE) != 0) {
            Logger.warn("Bridge is both required and disallowed.");
        }

        return this;
    }

    public MethodMatcher disallowBridge() {

        this.disallowedModifiers |= BRIDGE;
        if ((this.requiredModifiers & BRIDGE) != 0) {
            Logger.warn("Bridge is both required and disallowed.");
        }

        return this;
    }

    @Override
    boolean match(Method method) {
        return this.match(method.getModifiers(), method.getReturnType()) &&
                compare(method.getParameterTypes(), this.parametersTypes, this.requireExactMatch);
    }
}
