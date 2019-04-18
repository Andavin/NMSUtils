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

import com.andavin.inject.MinecraftInjector;
import com.andavin.util.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

/**
 * @since December 30, 2018
 * @author Andavin
 */
public class NBTBaseInjector extends com.andavin.inject.injectors.NBTBaseInjector {

    private static final String NBT_BASE = MINECRAFT_PREFIX + "NBTBase";
    private static final String NBT_DESC = "(B)L" + NBT_BASE + ';';
    private static final String METHOD_NAME = "createTag", FIELD_NAME = "a";
    private static final String VERSION_DESC = VERSION_PREFIX + "1.0"; // Version 1.0

    public NBTBaseInjector() {
        MinecraftInjector.injectClass(new NBTTagLongArray());
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
            } else if ((method.access & ACC_PROTECTED) != 0 && method.name.equals(METHOD_NAME) && method.desc.equals(NBT_DESC)) {

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

        if (clinitVisitor == null) {
            Logger.warn("Could not find <clinit> method.");
            return false;
        }

        if (methodVisitor == null) {
            Logger.warn("Could not find method with protected access with the name {} and descriptor {}.", METHOD_NAME, NBT_DESC);
            return false;
        }

        clinitVisitor.visitIntInsn(BIPUSH, 13);
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
        methodVisitor.visitVarInsn(ILOAD, 0);
        Label case0 = new Label();
        Label case1 = new Label();
        Label case2 = new Label();
        Label case3 = new Label();
        Label case4 = new Label();
        Label case5 = new Label();
        Label case6 = new Label();
        Label case7 = new Label();
        Label case8 = new Label();
        Label case9 = new Label();
        Label case10 = new Label();
        Label case11 = new Label();
        Label case12 = new Label();
        Label defaultCase = new Label();

        // Create switch
        methodVisitor.visitTableSwitchInsn(0, 12, defaultCase, case0, case1, case2, case3, case4, case5,
                case6, case7, case8, case9, case10, case11, case12);

        methodVisitor.visitLabel(case0);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagEnd");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagEnd", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case1);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagByte");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagByte", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case2);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagShort");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagShort", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case3);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagInt");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagInt", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case4);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagLong");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagLong", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case5);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagFloat");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagFloat", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case6);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagDouble");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagDouble", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case7);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagByteArray");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagByteArray", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case8);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagString");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagString", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case9);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagList");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagList", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case10);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagCompound");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagCompound", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case11);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagIntArray");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagIntArray", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(case12);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitTypeInsn(NEW, MINECRAFT_PREFIX + "NBTTagLongArray");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, MINECRAFT_PREFIX + "NBTTagLongArray", "<init>", "()V", false);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(defaultCase);
        methodVisitor.visitFrame(F_SAME, 0, null, 0, null);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitAnnotation(VERSION_DESC, false); // Add version
        return true;
    }
}
