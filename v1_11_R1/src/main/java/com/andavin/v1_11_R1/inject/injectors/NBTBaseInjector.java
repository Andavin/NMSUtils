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

package com.andavin.v1_11_R1.inject.injectors;

import com.andavin.inject.MinecraftInjector;
import com.andavin.util.Logger;
import net.minecraft.server.v1_11_R1.NBTTagLongArray;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

/**
 * @since December 30, 2018
 * @author Andavin
 */
public class NBTBaseInjector extends com.andavin.inject.injectors.NBTBaseInjector {

    private static final String NBT_BASE = MINECRAFT_PREFIX + "NBTBase";
    private static final String NBT_DESC = "(B)L" + NBT_BASE + ';';
    private static final String METHOD_NAME = "createTag", FIELD_NAME = "a";
    private static final String VERSION_DESC = VERSION_PREFIX + "1.0"; // Version 1.0

    public NBTBaseInjector(Plugin plugin) {
        MinecraftInjector.injectClass(plugin, NBTTagLongArray.class);
    }

    @Override
    public boolean inject(ClassNode node) {

        MethodNode methodVisitor = null, clinitVisitor = null;
        ListIterator<MethodNode> itr = node.methods.listIterator();
        while (itr.hasNext()) {

            MethodNode method = itr.next();
            if ((method.access & ACC_STATIC) != 0 && method.name.equals("<clinit>")) {
                clinitVisitor = new MethodNode(ASM7, method.access, method.name, method.desc, null, null);
                itr.set(clinitVisitor);
            } else if ((method.access & ACC_PUBLIC) != 0 && method.name.equals(METHOD_NAME) && method.desc.equals(NBT_DESC)) {

                if (method.invisibleAnnotations != null) {

                    for (AnnotationNode annotation : method.invisibleAnnotations) {

                        if (annotation.desc.equals(VERSION_DESC)) {
                            return false;
                        }
                    }
                }

                methodVisitor = new MethodNode(ASM7, method.access, method.name, method.desc, null, null);
                itr.set(methodVisitor);
            }

            if (methodVisitor != null && clinitVisitor != null) {
                break;
            }
        }

        if (methodVisitor == null || clinitVisitor == null) {
            Logger.warn("Could not find method with public access with the name {} and descriptor {}.", METHOD_NAME, NBT_DESC);
            return false;
        }

        Label label0 = new Label();
        clinitVisitor.visitLabel(label0);
        clinitVisitor.visitIntInsn(BIPUSH, 12);
        clinitVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitInsn(ICONST_0);
        clinitVisitor.visitLdcInsn("END");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitInsn(ICONST_1);
        clinitVisitor.visitLdcInsn("BYTE");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitInsn(ICONST_2);
        clinitVisitor.visitLdcInsn("SHORT");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitInsn(ICONST_3);
        clinitVisitor.visitLdcInsn("INT");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitInsn(ICONST_4);
        clinitVisitor.visitLdcInsn("LONG");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitInsn(ICONST_5);
        clinitVisitor.visitLdcInsn("FLOAT");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 6);
        clinitVisitor.visitLdcInsn("DOUBLE");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 7);
        clinitVisitor.visitLdcInsn("BYTE[]");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 8);
        clinitVisitor.visitLdcInsn("STRING");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 9);
        clinitVisitor.visitLdcInsn("LIST");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 10);
        clinitVisitor.visitLdcInsn("COMPOUND");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 11);
        clinitVisitor.visitLdcInsn("INT[]");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitInsn(DUP);
        clinitVisitor.visitIntInsn(BIPUSH, 12);
        clinitVisitor.visitLdcInsn("LONG[]");
        clinitVisitor.visitInsn(AASTORE);
        clinitVisitor.visitFieldInsn(PUTSTATIC, NBT_BASE, "a", "[Ljava/lang/String;");
        clinitVisitor.visitInsn(RETURN);

        if (methodVisitor.invisibleAnnotations != null) { // Remove older versions
            methodVisitor.invisibleAnnotations.removeIf(annotation -> annotation.desc.startsWith(VERSION_PREFIX));
        }

        methodVisitor.visitCode();
        Label l0 = new Label();
        methodVisitor.visitLabel(l0);
        methodVisitor.visitVarInsn(ILOAD, 0);
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        Label l5 = new Label();
        Label l6 = new Label();
        Label l7 = new Label();
        Label l8 = new Label();
        Label l9 = new Label();
        Label l10 = new Label();
        Label l11 = new Label();
        Label l12 = new Label();
        Label l13 = new Label();
        Label l14 = new Label();
        methodVisitor.visitTableSwitchInsn(0, 11, l14, l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12, l13);
        methodVisitor.visitLabel(l1);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagEnd");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagEnd", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l2);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagByte");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagByte", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l3);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagShort");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagShort", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l4);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagInt");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagInt", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l5);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagLong");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagLong", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l6);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagFloat");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagFloat", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l7);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagDouble");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagDouble", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l8);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagByteArray");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagByteArray", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l9);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagString");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagString", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l10);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagList");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagList", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l11);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagCompound");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagCompound", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l12);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagIntArray");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagIntArray", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l13);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagLongArray");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagLongArray", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(l14);

        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ARETURN);

        Label l15 = new Label();
        methodVisitor.visitLabel(l15);
        methodVisitor.visitLocalVariable("b0", "B", null, l0, l15, 0);
        methodVisitor.visitAnnotation(VERSION_DESC, false); // Add version
        return true;
    }
}
