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

package com.andavin.inject;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

/**
 * An injector specifically designed for injecting classes
 * that cannot be recognized as a class during compile time,
 * but instead are written purely at runtime.
 * <p>
 * The child class must be annotated with the {@link InjectorVersion}
 * annotation to signify its version. If the version is the same
 * as the class found in the JAR, then it will not be replaced.
 * <p>
 * The class will be injected directly using the bytes given that
 * describe it. The bytes must be structured according to the Java
 * language specification for a class. If the class is malformed in
 * any way, an error will not be thrown here and instead will be
 * given during runtime when the class is referenced or not at all
 * if the class exists elsewhere as a functioning version.
 *
 * @see InjectorVersion
 * @since January 23, 2019
 * @author Andavin
 */
public abstract class ByteClassInjector implements Injector { // implement simply for ease of access to static fields

    private final String internalName, version;

    /**
     * Create a new injector for a class that should be injected
     * directly using the bytes that makeup the class.
     *
     * @param internalName The internal name of the class to be injected.
     * @throws IllegalStateException If the class is not annotated
     *                               with {@link InjectorVersion}.
     */
    protected ByteClassInjector(String internalName) throws IllegalStateException {

        InjectorVersion version = this.getClass().getDeclaredAnnotation(InjectorVersion.class);
        if (version == null) {
            throw new IllegalStateException("Byte class injectors must be annotated with a version for their class");
        }

        this.version = version.value();
        this.internalName = internalName;
    }

    @Override
    public final boolean inject(ClassNode node) {
        return false;
    }

    /**
     * Get the internal name of the class that is represented
     * by the bytes that this class will be used to inject
     * (i.e. the name of the class being written).
     *
     * @return The internal name of the byte class.
     */
    String getInternalName() {
        return internalName;
    }

    /**
     * Get the version of the class that is represented by the
     * bytes that this class will be used to inject.
     *
     * @return The version of the byte class.
     */
    String getVersion() {
        return version;
    }

    /**
     * Write the version of the injector class to the given
     * {@link ClassWriter} so that it will be visible on the end class.
     *
     * @param writer The ClassWriter to write the version to.
     */
    protected void writeVersion(ClassWriter writer) {
        AnnotationVisitor annotation = writer.visitAnnotation("Lcom/andavin/inject/InjectorVersion;", true);
        annotation.visit("value", this.version);
        annotation.visitEnd();
    }

    /**
     * Write and dump all of the bytes that make up the structure
     * of the class to inject.
     * <p>
     * The class will be injected directly using the bytes given that
     * describe it. The bytes must be structured according to the Java
     * language specification for a class. If the class is malformed in
     * any way, an error will not be thrown here and instead will be
     * given during runtime when the class is referenced or not at all
     * if the class exists elsewhere as a functioning version.
     *
     * @return The bytes for the class to inject.
     * @throws IOException If anything goes wrong during the class
     *                     writing process.
     */
    protected abstract byte[] dump() throws IOException;
}
