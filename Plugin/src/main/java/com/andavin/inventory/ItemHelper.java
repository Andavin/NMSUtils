package com.andavin.inventory;

import com.andavin.nbt.ItemNBT;
import com.andavin.nbt.wrapper.NBTHelper;
import com.andavin.nbt.wrapper.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.andavin.reflect.Reflection.*;

/**
 * A basic helper class to do common tasks that have to do
 * with {@link ItemStack} such as checking if the
 * {@link #isEmpty(ItemStack) item is nothing} or
 * {@link #serialize(ItemStack) serializing} the item.
 *
 * @since November 07, 2018
 * @author Andavin
 * @see ItemNBT
 */
public final class ItemHelper {

    private static final Class<?> CRAFT_ITEM = findCraftClass("inventory.CraftItemStack");
    private static final Field HANDLE = findField(CRAFT_ITEM, "handle");

    private static final Method CRAFT_COPY = findMethod(CRAFT_ITEM, "asCraftCopy", ItemStack.class);
    private static final Method NMS_COPY = findMethod(CRAFT_ITEM, "asNMSCopy", ItemStack.class);

    private static final Executable CREATE_ITEM;
    private static final Method SAVE, CRAFT_MIRROR;

    static {
        Class<?> itemStack = findMcClass("ItemStack");
        Class<?> compound = findMcClass("NBTTagCompound");
        SAVE = findMethod(itemStack, "save", compound);
        CRAFT_MIRROR = findMethod(CRAFT_ITEM, "asCraftMirror", itemStack);
        CREATE_ITEM = VERSION_NUMBER >= 1100 ?
                findConstructor(itemStack, false, compound) :
                findMethod(itemStack, "createStack", false, compound);
    }

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
        return CRAFT_ITEM.isInstance(item) ? item : invoke(CRAFT_COPY, null, item);
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
        //noinspection ConstantConditions
        return CRAFT_ITEM.isInstance(item) ? getValue(HANDLE, item) :
                invoke(NMS_COPY, null, item);
    }

    /**
     * Serialize the given {@link ItemStack} into a byte array
     * that can be used to fully reconstruct the ItemStack later on.
     *
     * @param item The item to serialize.
     * @return The byte array for the serialized item.
     * @throws UncheckedIOException If something goes wrong during serialization.
     * @see #deserialize(byte[])
     */
    public static byte[] serialize(ItemStack item) throws UncheckedIOException {

        Object nmsItem = getNmsItemStack(item);
        Object tag = invoke(SAVE, nmsItem, NBTHelper.createTag(NBTTagCompound.class));
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            NBTHelper.serialize(stream, tag);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Deserialize the given byte array back into an {@link ItemStack}.
     * Ideally, the array should come from the {@link #serialize(ItemStack)}
     * method, but any form of NBT serialized item should technically work here.
     *
     * @param bytes The bytes to deserialize.
     * @return The newly created ItemStack for the bytes.
     * @throws UncheckedIOException If something goes wrong during deserialization.
     * @see #serialize(ItemStack)
     */
    public static ItemStack deserialize(byte[] bytes) throws UncheckedIOException {

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            Object tag = NBTHelper.deserializeNMS(stream);
            return invoke(CRAFT_MIRROR, null, CREATE_ITEM.getClass() == Constructor.class ?
                    newInstance((Constructor) CREATE_ITEM, tag) : invoke((Method) CREATE_ITEM, null, tag));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
