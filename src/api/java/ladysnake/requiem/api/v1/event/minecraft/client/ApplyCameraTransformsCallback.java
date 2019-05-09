/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.event.minecraft.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.Camera;

public interface ApplyCameraTransformsCallback {

    void applyCameraTransformations(Camera camera, float tickDelta);

    Event<ApplyCameraTransformsCallback> EVENT = EventFactory.createArrayBacked(ApplyCameraTransformsCallback.class,
            (listeners) -> (camera, tickDelta) -> {
                for (ApplyCameraTransformsCallback handler : listeners) {
                    handler.applyCameraTransformations(camera, tickDelta);
                }
            });

}