package com.andavin.util;

import com.andavin.reflect.Reflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A class used exclusively for sending packets to
 * {@link Player Minecraft clients}.
 * <p>
 * This class uses the most common procedure for sending
 * packets to clients through the {@code PlayerConnection}.
 * This allows for Minecraft to process the packet normally
 * and not bypass any regular checks.
 *
 * @since May 29, 2018
 * @author Andavin
 */
public final class PacketSender {

    private static final Field CONNECTION;
    private static final Method SEND_PACKET, HANDLE;

    static {
        CONNECTION = Reflection.getField(Reflection.getMcClass("EntityPlayer"), "playerConnection");
        SEND_PACKET = Reflection.getMethod(Reflection.getMcClass("PlayerConnection"),
                "sendPacket", Reflection.getMcClass("Packet"));
        HANDLE = Reflection.getMethod(Reflection.getCraftClass("entity.CraftPlayer"), "getHandle");
    }

    /**
     * Send the given packet to the given {@link Player}'s client.
     * The packet will be sent through the player's connection.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#getInstance(Class, Object...)}
     * and {@link Reflection#getMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to send the packet to.
     * @param packet The packet instance to send.
     * @see Reflection
     */
    public static void sendPacket(final Player player, final Object packet) {
        final Object conn = Reflection.getValue(CONNECTION, Reflection.invokeMethod(HANDLE, player));
        Reflection.invokeMethod(SEND_PACKET, conn, packet);
    }

    /**
     * Send the given packets to the given {@link Player}'s client.
     * The packets will be sent through the player's connection.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#getInstance(Class, Object...)}
     * and {@link Reflection#getMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to send the packet to.
     * @param packets The list of packet instances to send.
     * @see Reflection
     */
    public static void sendPackets(final Player player, final List<Object> packets) {
        final Object conn = Reflection.getValue(CONNECTION, Reflection.invokeMethod(HANDLE, player));
        packets.forEach(packet -> Reflection.invokeMethod(SEND_PACKET, conn, packet));
    }
}
