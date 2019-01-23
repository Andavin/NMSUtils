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

package com.andavin.v1_9_R2.inject.injectors;

import com.andavin.inject.ByteClassInjector;
import com.andavin.inject.InjectorVersion;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;

/**
 * @since January 23, 2019
 * @author Andavin
 */
@InjectorVersion("1.0")
public final class NBTTagLongArray extends ByteClassInjector implements Opcodes {

    private static final String NAME = "net/minecraft/server/v1_9_R2/NBTTagLongArray";
    private static final String NBT_BASE = "net/minecraft/server/v1_9_R2/NBTBase";
    private static final String READ_LIMITER = "net/minecraft/server/v1_9_R2/NBTReadLimiter";

    NBTTagLongArray() {
        super(NAME);
    }

    @Override
    protected byte[] dump() throws IOException {

        ClassWriter writer = new ClassWriter(0); // Compute nothing
        writer.visit(V1_8, ACC_PUBLIC + ACC_SUPER, NAME, null, NBT_BASE, null);
        writer.visitSource("NBTTagLongArray.java", null);

        this.writeVersion(writer);
        writer.visitField(ACC_PRIVATE, "b", "[J", null, null).visitEnd();

        { // NBTTagLongArray()
            MethodVisitor method = writer.visitMethod(0, "<init>", "()V", null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(16, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKESPECIAL, NBT_BASE, "<init>", "()V", false);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(17, l1);
            method.visitInsn(RETURN);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l2, 0);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        { // NBTTagLongArray(long[])
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>", "([J)V", null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(19, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKESPECIAL, NBT_BASE, "<init>", "()V", false);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(20, l1);
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitFieldInsn(PUTFIELD, NAME, "b", "[J");
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitLineNumber(21, l2);
            method.visitInsn(RETURN);
            Label l3 = new Label();
            method.visitLabel(l3);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l3, 0);
            method.visitLocalVariable("along", "[J", null, l0, l3, 1);
            method.visitMaxs(2, 2);
            method.visitEnd();
        }

        { // NBTTagLongArray(List<Long>)
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "<init>",
                    "(Ljava/util/List;)V", "(Ljava/util/List<Ljava/lang/Long;>;)V", null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(24, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKESTATIC, NAME, "a", "(Ljava/util/List;)[J", false);
            method.visitMethodInsn(INVOKESPECIAL, NAME, "<init>", "([J)V", false);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(25, l1);
            method.visitInsn(RETURN);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l2, 0);
            method.visitLocalVariable("list", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Long;>;", l0, l2, 1);
            method.visitMaxs(2, 2);
            method.visitEnd();
        }

        { // void write(DataOutput) throws IOException
            MethodVisitor method = writer.visitMethod(0, "write",
                    "(Ljava/io/DataOutput;)V", null, new String[]{ "java/io/IOException" });
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(30, l0);
            method.visitVarInsn(ALOAD, 1);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitInsn(ARRAYLENGTH);
            method.visitMethodInsn(INVOKEINTERFACE, "java/io/DataOutput", "writeInt", "(I)V", true);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(31, l1);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitVarInsn(ASTORE, 2);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitLineNumber(32, l2);
            method.visitVarInsn(ALOAD, 2);
            method.visitVarInsn(ASTORE, 3);
            method.visitVarInsn(ALOAD, 3);
            method.visitInsn(ARRAYLENGTH);
            method.visitVarInsn(ISTORE, 4);
            method.visitInsn(ICONST_0);
            method.visitVarInsn(ISTORE, 5);
            Label l3 = new Label();
            method.visitLabel(l3);
            method.visitFrame(Opcodes.F_FULL, 6, new Object[]{ NAME, "java/io/DataOutput",
                    "[J", "[J", Opcodes.INTEGER, Opcodes.INTEGER }, 0, new Object[]{});
            method.visitVarInsn(ILOAD, 5);
            method.visitVarInsn(ILOAD, 4);
            Label l4 = new Label();
            method.visitJumpInsn(IF_ICMPGE, l4);
            method.visitVarInsn(ALOAD, 3);
            method.visitVarInsn(ILOAD, 5);
            method.visitInsn(LALOAD);
            method.visitVarInsn(LSTORE, 6);
            Label l5 = new Label();
            method.visitLabel(l5);
            method.visitLineNumber(33, l5);
            method.visitVarInsn(ALOAD, 1);
            method.visitVarInsn(LLOAD, 6);
            method.visitMethodInsn(INVOKEINTERFACE, "java/io/DataOutput", "writeLong", "(J)V", true);
            Label l6 = new Label();
            method.visitLabel(l6);
            method.visitLineNumber(32, l6);
            method.visitIincInsn(5, 1);
            method.visitJumpInsn(GOTO, l3);
            method.visitLabel(l4);
            method.visitLineNumber(35, l4);
            method.visitFrame(Opcodes.F_CHOP, 3, null, 0, null);
            method.visitInsn(RETURN);
            Label l7 = new Label();
            method.visitLabel(l7);
            method.visitLocalVariable("k", "J", null, l5, l6, 6);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l7, 0);
            method.visitLocalVariable("output", "Ljava/io/DataOutput;", null, l0, l7, 1);
            method.visitLocalVariable("array", "[J", null, l2, l7, 2);
            method.visitMaxs(3, 8);
            method.visitEnd();
        }

        { // void load(DataInput, int, NBTReadLimiter) throws IOException
            MethodVisitor method = writer.visitMethod(0, "load", "(Ljava/io/DataInput;IL" + READ_LIMITER +
                    ";)V", null, new String[]{ "java/io/IOException" });
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(40, l0);
            method.visitVarInsn(ALOAD, 3);
            method.visitLdcInsn(192L);
            method.visitMethodInsn(INVOKEVIRTUAL, READ_LIMITER, "a", "(J)V", false);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(41, l1);
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKEINTERFACE, "java/io/DataInput", "readInt", "()I", true);
            method.visitVarInsn(ISTORE, 4);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitLineNumber(42, l2);
            method.visitVarInsn(ALOAD, 3);
            method.visitIntInsn(BIPUSH, 64);
            method.visitVarInsn(ILOAD, 4);
            method.visitInsn(IMUL);
            method.visitInsn(I2L);
            method.visitMethodInsn(INVOKEVIRTUAL, READ_LIMITER, "a", "(J)V", false);
            Label l3 = new Label();
            method.visitLabel(l3);
            method.visitLineNumber(43, l3);
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ILOAD, 4);
            method.visitIntInsn(NEWARRAY, T_LONG);
            method.visitFieldInsn(PUTFIELD, NAME, "b", "[J");
            Label l4 = new Label();
            method.visitLabel(l4);
            method.visitLineNumber(44, l4);
            method.visitInsn(ICONST_0);
            method.visitVarInsn(ISTORE, 5);
            Label l5 = new Label();
            method.visitLabel(l5);
            method.visitFrame(Opcodes.F_APPEND, 2, new Object[]{ Opcodes.INTEGER, Opcodes.INTEGER }, 0, null);
            method.visitVarInsn(ILOAD, 5);
            method.visitVarInsn(ILOAD, 4);
            Label l6 = new Label();
            method.visitJumpInsn(IF_ICMPGE, l6);
            Label l7 = new Label();
            method.visitLabel(l7);
            method.visitLineNumber(45, l7);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitVarInsn(ILOAD, 5);
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKEINTERFACE, "java/io/DataInput", "readLong", "()J", true);
            method.visitInsn(LASTORE);
            Label l8 = new Label();
            method.visitLabel(l8);
            method.visitLineNumber(44, l8);
            method.visitIincInsn(5, 1);
            method.visitJumpInsn(GOTO, l5);
            method.visitLabel(l6);
            method.visitLineNumber(47, l6);
            method.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            method.visitInsn(RETURN);
            Label l9 = new Label();
            method.visitLabel(l9);
            method.visitLocalVariable("k", "I", null, l5, l6, 5);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l9, 0);
            method.visitLocalVariable("datainput", "Ljava/io/DataInput;", null, l0, l9, 1);
            method.visitLocalVariable("i", "I", null, l0, l9, 2);
            method.visitLocalVariable("nbtreadlimiter", 'L' + READ_LIMITER + ';', null, l0, l9, 3);
            method.visitLocalVariable("length", "I", null, l2, l9, 4);
            method.visitMaxs(4, 6);
            method.visitEnd();
        }

        { // byte getTypeId()
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "getTypeId", "()B", null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(51, l0);
            method.visitIntInsn(BIPUSH, 12);
            method.visitInsn(IRETURN);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l1, 0);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        { // String toString()
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "toString",
                    "()Ljava/lang/String;", null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(56, l0);
            method.visitTypeInsn(NEW, "java/lang/StringBuilder");
            method.visitInsn(DUP);
            method.visitLdcInsn("[L;");
            method.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>",
                    "(Ljava/lang/String;)V", false);
            method.visitVarInsn(ASTORE, 1);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(58, l1);
            method.visitInsn(ICONST_0);
            method.visitVarInsn(ISTORE, 2);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitFrame(Opcodes.F_APPEND, 2, new Object[]{ "java/lang/StringBuilder", Opcodes.INTEGER }, 0, null);
            method.visitVarInsn(ILOAD, 2);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitInsn(ARRAYLENGTH);
            Label l3 = new Label();
            method.visitJumpInsn(IF_ICMPGE, l3);
            Label l4 = new Label();
            method.visitLabel(l4);
            method.visitLineNumber(60, l4);
            method.visitVarInsn(ILOAD, 2);
            Label l5 = new Label();
            method.visitJumpInsn(IFEQ, l5);
            Label l6 = new Label();
            method.visitLabel(l6);
            method.visitLineNumber(61, l6);
            method.visitVarInsn(ALOAD, 1);
            method.visitIntInsn(BIPUSH, 44);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(C)Ljava/lang/StringBuilder;", false);
            method.visitInsn(POP);
            method.visitLabel(l5);
            method.visitLineNumber(64, l5);
            method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            method.visitVarInsn(ALOAD, 1);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitVarInsn(ILOAD, 2);
            method.visitInsn(LALOAD);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(J)Ljava/lang/StringBuilder;", false);
            method.visitIntInsn(BIPUSH, 76);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(C)Ljava/lang/StringBuilder;", false);
            method.visitInsn(POP);
            Label l7 = new Label();
            method.visitLabel(l7);
            method.visitLineNumber(58, l7);
            method.visitIincInsn(2, 1);
            method.visitJumpInsn(GOTO, l2);
            method.visitLabel(l3);
            method.visitLineNumber(67, l3);
            method.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            method.visitVarInsn(ALOAD, 1);
            method.visitIntInsn(BIPUSH, 93);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(C)Ljava/lang/StringBuilder;", false);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                    "()Ljava/lang/String;", false);
            method.visitInsn(ARETURN);
            Label l8 = new Label();
            method.visitLabel(l8);
            method.visitLocalVariable("i", "I", null, l2, l3, 2);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l8, 0);
            method.visitLocalVariable("stringbuilder", "Ljava/lang/StringBuilder;", null, l1, l8, 1);
            method.visitMaxs(3, 3);
            method.visitEnd();
        }

        { // NBTBase c()
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "c",
                    "()L" + NBT_BASE + ';', null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(71, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitInsn(ARRAYLENGTH);
            method.visitIntInsn(NEWARRAY, T_LONG);
            method.visitVarInsn(ASTORE, 1);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(72, l1);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitInsn(ICONST_0);
            method.visitVarInsn(ALOAD, 1);
            method.visitInsn(ICONST_0);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitInsn(ARRAYLENGTH);
            method.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy",
                    "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitLineNumber(73, l2);
            method.visitTypeInsn(NEW, NAME);
            method.visitInsn(DUP);
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKESPECIAL, NAME, "<init>", "([J)V", false);
            method.visitInsn(ARETURN);
            Label l3 = new Label();
            method.visitLabel(l3);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l3, 0);
            method.visitLocalVariable("along", "[J", null, l1, l3, 1);
            method.visitMaxs(5, 2);
            method.visitEnd();
        }

        { // boolean equals(Object)
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(78, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ALOAD, 1);
            method.visitMethodInsn(INVOKESPECIAL, NBT_BASE, "equals",
                    "(Ljava/lang/Object;)Z", false);
            Label l1 = new Label();
            method.visitJumpInsn(IFEQ, l1);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitVarInsn(ALOAD, 1);
            method.visitTypeInsn(CHECKCAST, NAME);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "equals", "([J[J)Z", false);
            method.visitJumpInsn(IFEQ, l1);
            method.visitInsn(ICONST_1);
            Label l2 = new Label();
            method.visitJumpInsn(GOTO, l2);
            method.visitLabel(l1);
            method.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            method.visitInsn(ICONST_0);
            method.visitLabel(l2);
            method.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{ Opcodes.INTEGER });
            method.visitInsn(IRETURN);
            Label l3 = new Label();
            method.visitLabel(l3);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l3, 0);
            method.visitLocalVariable("object", "Ljava/lang/Object;", null, l0, l3, 1);
            method.visitMaxs(2, 2);
            method.visitEnd();
        }

        { // int hashCode()
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(83, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKESPECIAL, NBT_BASE, "hashCode", "()I", false);
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, NAME, "b", "[J");
            method.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "hashCode", "([J)I", false);
            method.visitInsn(IXOR);
            method.visitInsn(IRETURN);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l1, 0);
            method.visitMaxs(2, 1);
            method.visitEnd();
        }

        { // NBTBase clone()
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC, "clone",
                    "()L" + NBT_BASE + ';', null, null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(88, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKEVIRTUAL, NAME, "c", "()L" + NBT_BASE + ';', false);
            method.visitInsn(ARETURN);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l1, 0);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        { // long[] a(List<Long>)
            MethodVisitor method = writer.visitMethod(ACC_PRIVATE + ACC_STATIC, "a",
                    "(Ljava/util/List;)[J", "(Ljava/util/List<Ljava/lang/Long;>;)[J", null);
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(93, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
            method.visitIntInsn(NEWARRAY, T_LONG);
            method.visitVarInsn(ASTORE, 1);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLineNumber(95, l1);
            method.visitInsn(ICONST_0);
            method.visitVarInsn(ISTORE, 2);
            Label l2 = new Label();
            method.visitLabel(l2);
            method.visitFrame(Opcodes.F_APPEND, 2, new Object[]{ "[J", Opcodes.INTEGER }, 0, null);
            method.visitVarInsn(ILOAD, 2);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
            Label l3 = new Label();
            method.visitJumpInsn(IF_ICMPGE, l3);
            Label l4 = new Label();
            method.visitLabel(l4);
            method.visitLineNumber(96, l4);
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(ILOAD, 2);
            method.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
            method.visitTypeInsn(CHECKCAST, "java/lang/Long");
            method.visitVarInsn(ASTORE, 3);
            Label l5 = new Label();
            method.visitLabel(l5);
            method.visitLineNumber(97, l5);
            method.visitVarInsn(ALOAD, 1);
            method.visitVarInsn(ILOAD, 2);
            method.visitVarInsn(ALOAD, 3);
            Label l6 = new Label();
            method.visitJumpInsn(IFNONNULL, l6);
            method.visitInsn(LCONST_0);
            Label l7 = new Label();
            method.visitJumpInsn(GOTO, l7);
            method.visitLabel(l6);
            method.visitFrame(Opcodes.F_FULL, 4, new Object[]{ "java/util/List", "[J", Opcodes.INTEGER, "java/lang/Long" },
                    2, new Object[]{ "[J", Opcodes.INTEGER });
            method.visitVarInsn(ALOAD, 3);
            method.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            method.visitLabel(l7);
            method.visitFrame(Opcodes.F_FULL, 4, new Object[]{ "java/util/List", "[J", Opcodes.INTEGER, "java/lang/Long" },
                    3, new Object[]{ "[J", Opcodes.INTEGER, Opcodes.LONG });
            method.visitInsn(LASTORE);
            Label l8 = new Label();
            method.visitLabel(l8);
            method.visitLineNumber(95, l8);
            method.visitIincInsn(2, 1);
            method.visitJumpInsn(GOTO, l2);
            method.visitLabel(l3);
            method.visitLineNumber(100, l3);
            method.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
            method.visitVarInsn(ALOAD, 1);
            method.visitInsn(ARETURN);
            Label l9 = new Label();
            method.visitLabel(l9);
            method.visitLocalVariable("l", "Ljava/lang/Long;", null, l5, l8, 3);
            method.visitLocalVariable("i", "I", null, l2, l3, 2);
            method.visitLocalVariable("list", "Ljava/util/List;", "Ljava/util/List<Ljava/lang/Long;>;", l0, l9, 0);
            method.visitLocalVariable("array", "[J", null, l1, l9, 1);
            method.visitMaxs(4, 4);
            method.visitEnd();
        }

        { // Object clone() - a synthetic method created by the compiler
            MethodVisitor method = writer.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "clone",
                    "()Ljava/lang/Object;", null, new String[]{ "java/lang/CloneNotSupportedException" });
            method.visitCode();
            Label l0 = new Label();
            method.visitLabel(l0);
            method.visitLineNumber(11, l0);
            method.visitVarInsn(ALOAD, 0);
            method.visitMethodInsn(INVOKEVIRTUAL, NAME, "clone", "()L" + NBT_BASE + ';', false);
            method.visitInsn(ARETURN);
            Label l1 = new Label();
            method.visitLabel(l1);
            method.visitLocalVariable("this", 'L' + NAME + ';', null, l0, l1, 0);
            method.visitMaxs(1, 1);
            method.visitEnd();
        }

        writer.visitEnd();
        return writer.toByteArray();
    }
}
