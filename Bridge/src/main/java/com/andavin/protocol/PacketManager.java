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

package com.andavin.protocol;

import com.andavin.Versioned;

/**
 * @since November 18, 2018
 * @author Andavin
 */
public abstract class PacketManager implements Versioned {

    protected final ProtocolManager packetManager;

    protected PacketManager(ProtocolManager packetManager) {
        this.packetManager = packetManager;
    }

    /**
     * Inject the protocol listener into the system and
     * start listening to all incoming and outgoing packets.
     * <p>
     * This and {@link #stop()} can be called multiple
     * times to start and stop the packet listening process.
     */
    public abstract void start();

    /**
     * Close the protocol listener forcing it to stop
     * listening to incoming and outgoing packets.
     * <p>
     * This and {@link #start()} can be called multiple
     * times to start and stop the packet listening process.
     */
    public abstract void stop();
}
