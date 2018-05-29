package com.andavin;

/**
 * A class that holds some type of data.
 *
 * @author Andavin
 * @since May 14, 2018
 */
@FunctionalInterface
public interface DataHolder<D> {

    /**
     * Get the data that is being held in this class.
     *
     * @return The data that is being held.
     */
    D getData();
}
