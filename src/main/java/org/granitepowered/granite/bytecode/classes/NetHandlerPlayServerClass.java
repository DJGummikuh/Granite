/*
 * License (MIT)
 *
 * Copyright (c) 2014-2015 Granite Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.granitepowered.granite.bytecode.classes;

import static org.granitepowered.granite.util.MinecraftUtils.wrap;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Throwables;
import org.granitepowered.granite.Granite;
import org.granitepowered.granite.bytecode.BytecodeClass;
import org.granitepowered.granite.bytecode.Proxy;
import org.granitepowered.granite.impl.entity.player.GranitePlayer;
import org.granitepowered.granite.impl.event.player.GranitePlayerChatEvent;
import org.granitepowered.granite.impl.event.player.GranitePlayerMoveEvent;
import org.granitepowered.granite.mappings.Mappings;
import org.granitepowered.granite.mc.*;
import org.spongepowered.api.world.Location;

import java.lang.reflect.Constructor;

public class NetHandlerPlayServerClass extends BytecodeClass {
    Constructor quickExitExceptionConstructor;

    public NetHandlerPlayServerClass() {
        super("NetHandlerPlayServer");
    }

    @Proxy(methodName = "processChatMessage")
    public Object processChatMessage(MCNetHandlerPlayServer caller, Object[] args, ProxyHandlerCallback callback) throws Throwable {
        quickExitThreadIfNotServer((MCPacket) args[0], caller);

        MCPacketChatMessage packet = (MCPacketChatMessage) args[0];
        String message = packet.fieldGet$message();

        GranitePlayer p = wrap(caller.fieldGet$playerEntity());
        GranitePlayerChatEvent event = new GranitePlayerChatEvent(p, message);
        Granite.getInstance().getEventManager().post(event);

        return callback.invokeParent(args);
    }

    @Proxy(methodName = "processPlayer")
    public Object processPlayer(MCNetHandlerPlayServer caller, Object[] args, ProxyHandlerCallback callback) throws Throwable {
        quickExitThreadIfNotServer((MCPacket) args[0], caller);

        GranitePlayer player = wrap(caller.fieldGet$playerEntity());
        Location old = player.getLocation();
        Location new_ = new Location(old.getExtent(), new Vector3d(0, 0, 0));

        MCPacketPlayer packet = (MCPacketPlayer) args[0];

        // If setting position (for some reason MCP doesn't wanna name the field correctly)
        if (packet.fieldGet$field_149480_h()) {
            new_ = new_.add(packet.fieldGet$x(), packet.fieldGet$y(), packet.fieldGet$z());
        } else {
            new_ = new_.add(old.getPosition());
        }

        // TODO: Add yaw/pitch (if packet.rotating(), set y/p on new_)

        if (!old.getPosition().equals(new_.getPosition())) {
            GranitePlayerMoveEvent event = new GranitePlayerMoveEvent(player, old, new_);
            Granite.getInstance().getEventManager().post(event);

            if (!event.isCancelled()) {
                callback.invokeParent(args);
            }
        } else {
            callback.invokeParent(args);
        }
        return null;
    }

    @Override
    public void post() {
        super.post();

        try {
            quickExitExceptionConstructor = Mappings.getClass("ThreadQuickExitException").getDeclaredConstructor();
            quickExitExceptionConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            Throwables.propagate(e);
        }
    }

    public void quickExitThreadIfNotServer(MCPacket packet, MCNetHandlerPlayServer netHandlerPlayServer) {
        Mappings.invokeStatic(
                "PacketThreadUtil", "func_180031_a",
                packet, netHandlerPlayServer, netHandlerPlayServer.fieldGet$playerEntity().fieldGet$worldObj());
    }
}
