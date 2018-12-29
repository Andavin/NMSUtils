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

package com.andavin.v1_13_R2.inject;

import com.andavin.inject.MinecraftInjector;
import com.andavin.util.Logger;
import com.andavin.v1_13_R2.protocol.NetworkManagerProxy;
import com.andavin.v1_13_R2.protocol.ServerConnectionProxy;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * @since December 25, 2018
 * @author Andavin
 */
class MinecraftServerInjector extends com.andavin.inject.MinecraftServerInjector {

    private static final String MINECRAFT_SERVER = MINECRAFT_PREFIX + "MinecraftServer";
    private static final String SERVER_CONNECTION = MINECRAFT_PREFIX + "ServerConnection";
    private static final String PROXY = Type.getInternalName(ServerConnectionProxy.class);
    private static final String METHOD_NAME = "getServerConnection", FIELD_NAME = "serverConnection", DESC = 'L' + SERVER_CONNECTION + ';';
    private static final String VERSION_DESC = VERSION_PREFIX + "1.0"; // Version 1.0

    public MinecraftServerInjector(Plugin plugin) {
        MinecraftInjector.injectClass(plugin, ServerConnectionProxy.class);
        MinecraftInjector.injectClass(plugin, NetworkManagerProxy.class);
    }

    @Override
    public ClassWriter inject(ClassNode node, ClassReader reader) {

        MethodNode methodNode = null;
        ListIterator<MethodNode> itr = node.methods.listIterator();
        while (itr.hasNext()) {

            MethodNode method = itr.next();
            if ((method.access & ACC_PUBLIC) != 0 && method.name.equals(METHOD_NAME) && method.desc.endsWith(DESC)) {

                if (method.invisibleAnnotations != null) {

                    for (AnnotationNode annotation : method.invisibleAnnotations) {

                        if (annotation.desc.equals(VERSION_DESC)) {
                            return null;
                        }
                    }
                }

                methodNode = method;
                break;
            }
        }

        if (methodNode == null) {
            Logger.warn("Could not find method with public access with the name {} and descriptor {}.", METHOD_NAME, DESC);
            return null;
        }

        if (methodNode.invisibleAnnotations != null) { // Remove older versions
            methodNode.invisibleAnnotations.removeIf(annotation -> annotation.desc.startsWith(VERSION_PREFIX));
        }

        MethodNode visitor = new MethodNode(ASM7, methodNode.access, methodNode.name, methodNode.desc, null, null);
        itr.set(visitor); // Set the method to the new one

        Label l0 = new Label();
        visitor.visitLabel(l0);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, MINECRAFT_SERVER, FIELD_NAME, DESC);

        Label l1 = new Label();
        visitor.visitJumpInsn(IFNONNULL, l1);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitTypeInsn(NEW, PROXY);
        visitor.visitInsn(DUP);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(INVOKESPECIAL, PROXY, "<init>", "(L" + MINECRAFT_SERVER + ";)V", false);
        visitor.visitInsn(DUP_X1);
        visitor.visitFieldInsn(PUTFIELD, MINECRAFT_SERVER, FIELD_NAME, DESC);

        Label l2 = new Label();
        visitor.visitJumpInsn(GOTO, l2);
        visitor.visitLabel(l1);
        visitor.visitFrame(F_SAME, 0, null, 0, null);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, MINECRAFT_SERVER, FIELD_NAME, DESC);
        visitor.visitLabel(l2);
        visitor.visitFrame(F_SAME1, 0, null, 1, new Object[]{ SERVER_CONNECTION });
        visitor.visitInsn(ARETURN);

        Label l3 = new Label();
        visitor.visitLabel(l3);
        visitor.visitLocalVariable("this", "L" + MINECRAFT_SERVER + ";", null, l0, l3, 0);
        visitor.visitAnnotation(VERSION_DESC, false); // Add version
        visitor.visitMaxs(0, 0);

        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer;
    }
}
