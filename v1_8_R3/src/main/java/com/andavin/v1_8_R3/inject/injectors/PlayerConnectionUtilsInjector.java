///*
// * MIT License
// *
// * Copyright (c) 2018 Andavin
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package com.andavin.v1_8_R3.inject.injectors;
//
//import com.andavin.inject.Injector;
//import org.objectweb.asm.tree.ClassNode;
//
///**
// * @since April 17, 2019
// * @author Andavin
// */
//public class PlayerConnectionUtilsInjector implements Injector {
//
//    @Override
//    public boolean inject(ClassNode node) {
//        mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
//        mv.visitCode();
//        Label l0 = new Label();
//        mv.visitLabel(l0);
//        mv.visitLineNumber(17, l0);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$listener", "Lnet/minecraft/server/PacketListener;");
//        mv.visitTypeInsn(INSTANCEOF, "net/minecraft/server/PlayerConnection");
//        Label l1 = new Label();
//        mv.visitJumpInsn(IFEQ, l1);
//        Label l2 = new Label();
//        mv.visitLabel(l2);
//        mv.visitLineNumber(19, l2);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$listener", "Lnet/minecraft/server/PacketListener;");
//        mv.visitTypeInsn(CHECKCAST, "net/minecraft/server/PlayerConnection");
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnection", "networkManager", "Lnet/minecraft/server/NetworkManager;");
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/NetworkManager", "packetListener", "Ljava/util/function/BiFunction;");
//        mv.visitVarInsn(ASTORE, 1);
//        Label l3 = new Label();
//        mv.visitLabel(l3);
//        mv.visitLineNumber(20, l3);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitJumpInsn(IFNULL, l1);
//        Label l4 = new Label();
//        mv.visitLabel(l4);
//        mv.visitLineNumber(22, l4);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$listener", "Lnet/minecraft/server/PacketListener;");
//        mv.visitTypeInsn(CHECKCAST, "net/minecraft/server/PlayerConnection");
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnection", "player", "Lnet/minecraft/server/EntityPlayer;");
//        mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/server/EntityPlayer", "getBukkitEntity", "()Lorg/bukkit/craftbukkit/entity/CraftPlayer;", false);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$packet", "Lnet/minecraft/server/Packet;");
//        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/BiFunction", "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
//        mv.visitTypeInsn(CHECKCAST, "net/minecraft/server/Packet");
//        mv.visitVarInsn(ASTORE, 2);
//        Label l5 = new Label();
//        mv.visitLabel(l5);
//        mv.visitLineNumber(23, l5);
//        mv.visitVarInsn(ALOAD, 2);
//        Label l6 = new Label();
//        mv.visitJumpInsn(IFNONNULL, l6);
//        Label l7 = new Label();
//        mv.visitLabel(l7);
//        mv.visitLineNumber(24, l7);
//        mv.visitInsn(RETURN);
//        mv.visitLabel(l6);
//        mv.visitLineNumber(27, l6);
//        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{ "java/util/function/BiFunction", "net/minecraft/server/Packet" }, 0, null);
//        mv.visitVarInsn(ALOAD, 2);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$listener", "Lnet/minecraft/server/PacketListener;");
//        mv.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/server/Packet", "a", "(Lnet/minecraft/server/PacketListener;)V", true);
//        Label l8 = new Label();
//        mv.visitLabel(l8);
//        mv.visitLineNumber(28, l8);
//        mv.visitInsn(RETURN);
//        mv.visitLabel(l1);
//        mv.visitLineNumber(32, l1);
//        mv.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$packet", "Lnet/minecraft/server/Packet;");
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitFieldInsn(GETFIELD, "net/minecraft/server/PlayerConnectionUtils$1", "val$listener", "Lnet/minecraft/server/PacketListener;");
//        mv.visitMethodInsn(INVOKEINTERFACE, "net/minecraft/server/Packet", "a", "(Lnet/minecraft/server/PacketListener;)V", true);
//        Label l9 = new Label();
//        mv.visitLabel(l9);
//        mv.visitLineNumber(33, l9);
//        mv.visitInsn(RETURN);
//        Label l10 = new Label();
//        mv.visitLabel(l10);
//        mv.visitLocalVariable("other", "Lnet/minecraft/server/Packet;", null, l5, l1, 2);
//        mv.visitLocalVariable("packetListener", "Ljava/util/function/BiFunction;", "Ljava/util/function/BiFunction<Lorg/bukkit/entity/Player;Lnet/minecraft/server/Packet;Lnet/minecraft/server/Packet;>;", l3, l1, 1);
//        mv.visitLocalVariable("this", "Lnet/minecraft/server/PlayerConnectionUtils$1;", null, l0, l10, 0);
//        mv.visitMaxs(3, 3);
//        mv.visitEnd();
//    }
//}
