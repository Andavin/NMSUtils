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

package com.andavin;

import com.andavin.inject.MinecraftInjector;
import com.andavin.inject.injectors.MinecraftServerInjector;
import com.andavin.inject.injectors.NBTBaseInjector;
import com.andavin.nbt.wrapper.*;
import com.andavin.protocol.PacketListener;
import com.andavin.protocol.ProtocolManager;
import com.andavin.reflect.exception.UncheckedClassNotFoundException;
import com.andavin.util.Logger;
import com.andavin.util.MinecraftVersion;
import org.bukkit.plugin.java.JavaPlugin;

import static com.andavin.reflect.Reflection.findMcClass;
import static com.andavin.util.MinecraftVersion.v1_12;

public final class NMSUtils extends JavaPlugin {

    private static NMSUtils instance;
    private static boolean fastAsyncSupport;
    private ProtocolManager protocolManager;

    @Override
    public void onLoad() {

        instance = this;
        NBTHelper.register(
                NBTTagEnd.class,
                NBTTagByte.class,
                NBTTagShort.class,
                NBTTagInt.class,
                NBTTagLong.class,
                NBTTagFloat.class,
                NBTTagDouble.class,
                NBTTagByteArray.class,
                NBTTagIntArray.class,
                NBTTagCompound.class,
                NBTTagList.class,
                NBTTagString.class
        );

        try {
            NBTHelper.register(NBTTagLongArray.class);
        } catch (UncheckedClassNotFoundException e) {
            Logger.debug(e, "Registering NBTTagLongArray");
        }

        MinecraftInjector.register(findMcClass("MinecraftServer"),
                Versioned.getInstance(MinecraftServerInjector.class, this));
        if (MinecraftVersion.lessThan(v1_12)) {
            MinecraftInjector.register(findMcClass("NBTBase"),
                    Versioned.getInstance(NBTBaseInjector.class, this));
        }
    }

    @Override
    public void onEnable() {

        if (MinecraftInjector.inject()) {
            return;
        }

        this.protocolManager = Versioned.getInstance(ProtocolManager.class);
    }

    /**
     * The singleton instance of the {@link NMSUtils} plugin.
     *
     * @return This plugin instance.
     */
    public static NMSUtils getInstance() {
        return instance;
    }

    /**
     * Get the {@link ProtocolManager} that is used for
     * registering {@link PacketListener}s.
     *
     * @return The ProtocolManager.
     */
    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
