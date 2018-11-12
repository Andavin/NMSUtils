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

/**
 * This package contains all classes that directly hold NBT data.
 * These are fully capable NBT objects that can be easily transitioned
 * to their NMS counterparts.
 * <p>
 * You'll use these wrappers the same way you would with a regular NMS
 * NBT object, but without the need for the version dependency of NMS imports.
 * <br>
 * Wrappers will always maintain the same hierarchy as their NMS counterparts
 * including {@link com.andavin.nbt.wrapper.NBTNumber} and of course
 * {@link com.andavin.nbt.wrapper.NBTBase}.
 * <p>
 * Other useful classes can be found in this package also such as
 * {@link com.andavin.nbt.wrapper.NBTHelper} and also
 * {@link com.andavin.nbt.wrapper.NBTType} which help when
 * interacting with NMS itself as well as referencing obscure NMS magic
 * numbers and types.
 */
package com.andavin.nbt.wrapper;