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
package ladysnake.pandemonium.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * An efficient skin manager used to avoid freezes and rate limit issues caused by retrieving skin textures
 * from Mojang servers.
 * <p>
 * This class has been adapted from the Sync mod's <a href="https://github.com/iChun/Sync/blob/master/src/main/java/me/ichun/mods/sync/client/core/SyncSkinManager.java">source code</a>
 * under GNU Lesser General Public License.
 *
 * @author IChun
 */
public class RequiemSkinManager {
    //Cache skins throughout TEs to avoid hitting the rate limit for skin session servers
    //Hold values for a longer time, so they are loaded fast if many TEs with the same player are loaded, or when loading other chunks with the same player
    //Skin loading priority: Cache(fastest), ScoreboardEntry(only available when player is only and in same dim as shell, fast), SessionService(slow)
    private static final Cache<UUID, Identifier> skinCache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).build();
    private static final Set<UUID> queriedSkins = new HashSet<>();

    public static Identifier get(GameProfile profile) {
        Identifier loc = skinCache.getIfPresent(profile.getId());
        if (loc != null) {
            return loc;
        }
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        PlayerListEntry playerInfo = networkHandler == null ? null : networkHandler.getPlayerListEntry(profile.getId());
        if (playerInfo != null) { //load from network player
            loc = playerInfo.getSkinTexture();
            if (loc != DefaultSkinHelper.getTexture(playerInfo.getProfile().getId())) {
                skinCache.put(profile.getId(), loc);
                return loc;
            }
        }
        synchronized (queriedSkins) {
            if (!queriedSkins.contains(profile.getId())) {
                //Make one call per user - again rate limit protection
                MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, location, profileTexture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN) {
                        skinCache.put(profile.getId(), location);
                        synchronized (queriedSkins) {
                            queriedSkins.remove(profile.getId());
                        }
                    }
                }, true);
            }
            queriedSkins.add(profile.getId());
        }
        return DefaultSkinHelper.getTexture(profile.getId());
    }

    public static void invalidateCaches() {
        skinCache.invalidateAll();
        skinCache.cleanUp();
        queriedSkins.clear();
    }
}
