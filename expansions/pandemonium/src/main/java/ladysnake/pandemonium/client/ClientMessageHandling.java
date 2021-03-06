package ladysnake.pandemonium.client;

import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.requiem.client.RequiemFx;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.*;

public class ClientMessageHandling {
    private static final float[] ETHEREAL_DAMAGE_COLOR = {0.5f, 0.0f, 0.0f};

    public static void init() {
        ClientSidePacketRegistry.INSTANCE.register(ANCHOR_DAMAGE, ((context, buf) -> {
            boolean dead = buf.readBoolean();
            context.getTaskQueue().execute(() -> RequiemFx.INSTANCE.playEtherealPulseAnimation(
                dead ? 4 : 1, ETHEREAL_DAMAGE_COLOR[0], ETHEREAL_DAMAGE_COLOR[1], ETHEREAL_DAMAGE_COLOR[2]
            ));
        }));
        ClientSidePacketRegistry.INSTANCE.register(ANCHOR_SYNC, ((context, buf) -> {
            int id = buf.readInt();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            context.getTaskQueue().execute(() ->
                ((ClientAnchorManager) FractureAnchorManager.get(context.getPlayer().world)).getOrCreate(id).setPosition(x, y, z)
            );
        }));
        ClientSidePacketRegistry.INSTANCE.register(ANCHOR_REMOVE, ((context, buf) -> {
            int id = buf.readInt();
            context.getTaskQueue().execute(() -> {
                FractureAnchorManager manager = FractureAnchorManager.get(context.getPlayer().world);
                FractureAnchor anchor = manager.getAnchor(id);
                if (anchor != null) {
                    anchor.invalidate();
                }
            });
        }));
        ClientSidePacketRegistry.INSTANCE.register(ETHEREAL_ANIMATION, ((context, buf) -> context.getTaskQueue().execute(() -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            assert mc != null;
            assert mc.player != null;
            mc.player.world.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 2, 0.6f);
            RequiemFx.INSTANCE.beginEtherealAnimation();
        })));
    }

}
