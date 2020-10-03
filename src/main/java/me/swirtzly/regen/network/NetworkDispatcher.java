package me.swirtzly.regen.network;

import me.swirtzly.regen.network.messages.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static me.swirtzly.regen.util.RConstants.MODID;

public class NetworkDispatcher {

    public static SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> "1.0", "1.0"::equals, "1.0"::equals);

    public static void setUp() {
        int id = 0;
        NETWORK_CHANNEL.registerMessage(id++, SyncMessage.class, SyncMessage::toBytes, SyncMessage::new, SyncMessage::handle);
        NETWORK_CHANNEL.registerMessage(id++, SFXMessage.class, SFXMessage::toBytes, SFXMessage::new, SFXMessage::handle);
        NETWORK_CHANNEL.registerMessage(id++, POVMessage.class, POVMessage::toBytes, POVMessage::new, POVMessage::handle);
        NETWORK_CHANNEL.registerMessage(id++, StateMessage.class, StateMessage::toBytes, StateMessage::new, StateMessage::handle);
        NETWORK_CHANNEL.registerMessage(id++, SkinMessage.class, SkinMessage::toBytes, SkinMessage::new, SkinMessage::handle);
        NETWORK_CHANNEL.registerMessage(id++, ColorChangeMessage.class, ColorChangeMessage::toBytes, ColorChangeMessage::new, ColorChangeMessage::handle);
    }

}
