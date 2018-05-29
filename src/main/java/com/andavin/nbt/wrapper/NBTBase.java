package com.andavin.nbt.wrapper;

import com.andavin.DataHolder;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Collections;
import java.util.Map;

/**
 * A basic class that holds a wrapped NBT object
 * and that every NBT wrapper should extend to
 * give it basic NBT functionality and abstraction.
 *
 * @author Andavin
 * @since May 12, 2018
 */
public abstract class NBTBase implements ConfigurationSerializable {

    /**
     * This is the actual reference to the NMS NBT object.
     * Changes made to this object will be reflected in the
     * actual NBT object.
     */
    Object wrapped;
    private final byte typeId;

    /**
     * A constructor used to wrap the NMS NBT object. This basic
     * constructor should always exist in final child classes
     * in order to create objects by passing in a single NMS object.
     *
     * @param wrapped The NMS NBT object to wrap.
     */
    NBTBase(final Object wrapped) {
        this.wrapped = wrapped;
        final NBTTag type = this.getClass().getAnnotation(NBTTag.class);
        Preconditions.checkState(type != null, "{} missing tag annotation", this.getClass());
        this.typeId = type.typeId();
    }

    /**
     * The ID of the type of NBT that this NBT object is. This is
     * derived from and is the same ID as the wrapped NMS NBT type.
     *
     * @return The ID of this NBT type.
     * @see NBTType NBT Type IDs
     */
    public final byte getTypeId() {
        return typeId;
    }

    /**
     * Tell if this NBT has any values stored within its container.
     * <p>
     * If this object is any kind of number, then it will always
     * return false by default. If it is a list or compound (map),
     * then it will be based on if there is any objects held in
     * those specific storage containers.
     *
     * @return Tell if this object holds any values.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Get the wrapped NMS object (similar to {@code getHandle}
     * in CraftBukkit classes).
     *
     * @return The wrapped NMS NBT object.
     */
    public final Object getWrapped() {
        return wrapped;
    }

    @Override
    public final boolean equals(final Object o) {
        return o instanceof NBTBase && this.wrapped.equals(((NBTBase) o).wrapped);
    }

    @Override
    public final int hashCode() {
        return this.wrapped.hashCode();
    }

    @Override
    public String toString() {
        return this.wrapped.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        return this instanceof DataHolder ?
                Collections.singletonMap("data", ((DataHolder) this).getData()) :
                Collections.emptyMap();
    }
}
