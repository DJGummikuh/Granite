/*
 * License (MIT)
 *
 * Copyright (c) 2014 Granite Team
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

import com.flowpowered.math.vector.Vector3d;
import org.granitepowered.granite.Granite;
import org.granitepowered.granite.bytecode.BytecodeClass;
import org.granitepowered.granite.impl.block.GraniteBlockLoc;
import org.granitepowered.granite.impl.event.block.GraniteBlockBreakEvent;
import org.granitepowered.granite.impl.world.GraniteWorld;
import org.granitepowered.granite.mc.MCBlockPos;
import org.granitepowered.granite.mc.MCItemInWorldManager;
import org.spongepowered.api.world.Location;

import static org.granitepowered.granite.utils.MinecraftUtils.wrap;

public class ItemInWorldManagerClass extends BytecodeClass {
    public ItemInWorldManagerClass() {
        super("ItemInWorldManager");

        proxy("func_180237_b", new BytecodeClass.ProxyHandler() {
            @Override
            protected Object handle(Object caller, Object[] args, BytecodeClass.ProxyHandlerCallback callback) throws Throwable {
                MCItemInWorldManager thisIiwm = (MCItemInWorldManager) caller;

                MCBlockPos mcBlockPos = (MCBlockPos) args[0];
                Vector3d pos = new Vector3d(mcBlockPos.fieldGet$x(), mcBlockPos.fieldGet$y(), mcBlockPos.fieldGet$z());

                GraniteBlockBreakEvent event = new GraniteBlockBreakEvent(new GraniteBlockLoc(new Location((GraniteWorld) wrap(thisIiwm.fieldGet$theWorld()), pos)));
                Granite.getInstance().getServer().getEventManager().post(event);

                return callback.invokeParent(args);
            }
        });
    }
}
