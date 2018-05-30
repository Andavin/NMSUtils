package com.andavin;

import com.andavin.nbt.wrapper.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class NMSUtils extends JavaPlugin {

    private static NMSUtils instance;

    @Override
    public void onEnable() {
        instance = this;
        //noinspection unchecked
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
                NBTTagLongArray.class,
                NBTTagCompound.class,
                NBTTagList.class,
                NBTTagString.class
        );
    }

    /**
     * The singleton instance of the {@link NMSUtils} plugin.
     *
     * @return This plugin instance.
     */
    public static NMSUtils getInstance() {
        return instance;
    }
}
