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

package com.andavin.v1_8_R3.inject;

import com.andavin.util.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * @since December 25, 2018
 * @author Andavin
 */
class NetworkManagerInjector extends com.andavin.inject.NetworkManagerInjector {

    private static final String MINECRAFT_SERVER = "net/minecraft/server/v1_8_R3/MinecraftServer";
    private static final String SERVER_CONNECTION = "net/minecraft/server/v1_8_R3/ServerConnection";
    private static final String DELEGATE = "com/andavin/v1_8_R3/protocol/ServerConnectionDelegate";
    private static final String NAME = "aq", DESC = 'L' + SERVER_CONNECTION + ';';

    @Override
    public ClassWriter inject(ClassNode node, ClassReader reader) {

        MethodNode methodNode = null;
        ListIterator<MethodNode> itr = node.methods.listIterator();
        while (itr.hasNext()) {

            MethodNode method = itr.next();
            Logger.info("Method name {} desc {}", method.name, method.desc);
            if ((method.access & ACC_PUBLIC) != 0 && method.name.equals(NAME) && method.desc.endsWith(DESC)) {
                Logger.info("method locals {}", method.maxLocals);
                if (method.maxLocals == 3 && !method.tryCatchBlocks.isEmpty()) {
                    return null;
                }

                methodNode = method;
                break;
            }
        }

        if (methodNode == null) {
            Logger.warn("Could not find method with public access with the name {} and descriptor {}.", NAME, DESC);
            return null;
        }

        MethodNode visitor = new MethodNode(ASM7, methodNode.access, methodNode.name, methodNode.desc, null, null);
        itr.set(visitor); // Set the method to the new one

        Logger.info("Injecting method aq");
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        visitor.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
        Label l3 = new Label();
        visitor.visitLabel(l3);
        visitor.visitLineNumber(13, l3);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, MINECRAFT_SERVER, "q", DESC);
        Label l4 = new Label();
        visitor.visitJumpInsn(IFNULL, l4);

        Label l5 = new Label();
        visitor.visitLabel(l5);
        visitor.visitLineNumber(14, l5);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, MINECRAFT_SERVER, "q", DESC);
        visitor.visitInsn(ARETURN);
        visitor.visitLabel(l4);
        visitor.visitLineNumber(17, l4);
        visitor.visitFrame(F_SAME, 0, null, 0, null);
        visitor.visitTypeInsn(NEW, SERVER_CONNECTION);
        visitor.visitInsn(DUP);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(INVOKESPECIAL, SERVER_CONNECTION, "<init>",
                "(L" + MINECRAFT_SERVER + ";)V", false);

        visitor.visitVarInsn(ASTORE, 1);
        visitor.visitLabel(l0);
        visitor.visitLineNumber(19, l0);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitTypeInsn(NEW, DELEGATE);
        visitor.visitInsn(DUP);
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitMethodInsn(INVOKESPECIAL, DELEGATE, "<init>",
                "(" + DESC + ")V", false);
        visitor.visitInsn(DUP_X1);
        visitor.visitFieldInsn(PUTFIELD, MINECRAFT_SERVER, "q", DESC);
        visitor.visitLabel(l1);
        visitor.visitInsn(ARETURN);
        visitor.visitLabel(l2);
        visitor.visitLineNumber(20, l2);
        visitor.visitFrame(F_FULL, 2, new Object[]{ MINECRAFT_SERVER, SERVER_CONNECTION },
                1, new Object[]{ "java/lang/Exception" });
        visitor.visitVarInsn(ASTORE, 2);

        Label l6 = new Label();
        visitor.visitLabel(l6);
        visitor.visitLineNumber(21, l6);
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitInsn(DUP_X1);
        visitor.visitFieldInsn(PUTFIELD, MINECRAFT_SERVER, "q", DESC);
        visitor.visitInsn(ARETURN);

        Label l7 = new Label();
        visitor.visitLabel(l7);
        visitor.visitLocalVariable("ignored", "Ljava/lang/Exception;", null, l6, l7, 2);
        visitor.visitLocalVariable("this", 'L' + MINECRAFT_SERVER + ';', null, l3, l7, 0);
        visitor.visitLocalVariable("connection", DESC, null, l0, l7, 1);
        visitor.visitMaxs(4, 3);

        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer;
    }
}
