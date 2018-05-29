package com.andavin.nbt.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Andavin
 * @since May 12, 2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface NBTTag {

    /**
     * The integer identifier for the type of NBT tag that
     * is annotated. This ID should correspond to the NMS
     * NBT tag type IDs.
     *
     * @return The ID for this type of {@link NBTBase NBT tag}.
     */
    byte typeId();

    /**
     * The parameters needed to construct an NBT tag object
     * of the type that is wrapped by the annotated class.
     *
     * @return The types of the parameters required for the constructor.
     */
    Class<?>[] params() default {};
}
