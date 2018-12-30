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

import com.andavin.Versioned;
import com.andavin.util.MinecraftVersion;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * An injector for a class element. This will find a
 * class within a Minecraft or CraftBukkit JAR and
 * alter it using byte code manipulation.
 * <p>
 * Any classes that are not contained within the JAR
 * being altered can be injected into the JAR via the
 * {@link MinecraftInjector#injectClass(Plugin, Class)}
 * method and will write it to the JAR.
 *
 * @since December 25, 2018
 * @author Andavin
 */
@FunctionalInterface
public interface Injector extends Versioned {

    String VERSION_PREFIX = "InjectorVersion-";
    String MINECRAFT_PREFIX = MinecraftVersion.MINECRAFT_PREFIX.replace('.', '/');
    String CRAFTBUKKIT_PREFIX = MinecraftVersion.CRAFTBUKKIT_PREFIX.replace('.', '/');

    /**
     * Inject into the attribute specified by the implementation
     * and alter it according to the implementation.
     *
     * @param node The {@link ClassNode} to use to receive
     *             the information on the current state of
     *             the attribute.
     * @param reader The {@link ClassReader} to use to get all of
     *               the attributes of the attribute.
     * @return The altered {@link ClassWriter} or {@code null} if there
     *         were no alterations.
     */
    ClassWriter inject(ClassNode node, ClassReader reader);
}
