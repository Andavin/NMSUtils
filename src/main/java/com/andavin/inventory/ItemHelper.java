package com.andavin.inventory;

import com.andavin.reflect.Reflection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @since November 07, 2018
 * @author Andavin
 */
public final class ItemHelper {

    private static final Class<?> CRAFT_ITEM = Reflection.findCraftClass("inventory.CraftItemStack");
    private static final Field HANDLE = Reflection.findField(CRAFT_ITEM, "handle");

    private static final Method CRAFT_COPY = Reflection.findMethod(CRAFT_ITEM, "asCraftCopy", ItemStack.class);
    private static final Method NMS_COPY = Reflection.findMethod(CRAFT_ITEM, "asNMSCopy", ItemStack.class);

    /**
     * Check if the given {@link ItemStack item} is empty in that
     * if it is {@code null} or {@link Material#AIR air} it is empty.
     *
     * @param item The item in question.
     * @return If the item is empty.
     */
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Tell whether the given {@link ItemStack} is an instance
     * of {@code CraftItemStack} (the implementation of ItemStack
     * contained in the {@code inventory} package of CraftBukkit).
     *
     * @param item The ItemStack to test.
     * @return If the ItemStack is an instance of {@code CraftItemStack.}
     */
    public static boolean isCraftItem(ItemStack item) {
        return CRAFT_ITEM.isInstance(item);
    }

    /**
     * Ensure that the given {@link ItemStack} is an instance of
     * {@code CraftItemStack} and, if not, then return a new one.
     *
     * @param item The ItemStack to ensure.
     * @return The ItemStack that is an instance of {@code CraftItemStack}.
     */
    public static ItemStack ensureCraftItem(ItemStack item) {
        return CRAFT_ITEM.isInstance(item) ? item : Reflection.invoke(CRAFT_COPY, null, item);
    }

    /**
     * Get the {@code NMS ItemStack} equivalent of the Bukkit
     * version of the given {@link ItemStack item}.
     * <p>
     * Note that if the given Bukkit ItemStack was not an instance
     * of {@code CraftItemStack}, then the returned stack will not
     * be the one that is attached to the parameter and therefore
     * edits made to it may not be directly applicable to the parameter.
     *
     * @param item The item to get the NMS version for.
     * @return The NMS item stack.
     */
    public static Object getNmsItemStack(ItemStack item) {
        return CRAFT_ITEM.isInstance(item) ? Reflection.getValue(HANDLE, item) :
                Reflection.invoke(NMS_COPY, null, item);
    }

//    public static byte[] serialize(ItemStack item) throws UncheckedIOException {
//
//        Object nmsItem = getNmsItemStack(item);
//        NBTTagCompound tag = ((net.minecraft.server.v1_8_R3.ItemStack) nmsItem).save(new NBTTagCompound());
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        try {
//            NBTCompressedStreamTools.a(tag, stream);
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//
//        return stream.toByteArray();
//    }
//
//    public static ItemStack deserialize(byte[] bytes) {
//
//        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
//        NBTTagCompound tag;
//        try {
//            tag = NBTCompressedStreamTools.a(stream);
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//
//        return CraftItemStack.asCraftMirror(new net.minecraft.server.v1_11_R1.ItemStack(tag));
//    }
}
