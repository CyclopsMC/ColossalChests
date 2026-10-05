package org.cyclops.colossalchests2.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.cyclops.colossalchests2.ColossalChestsForge;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.ClientProxyComponentForge;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxyForge extends ClientProxyComponentForge {

    public ClientProxyForge() {
        super(new CommonProxyForge());
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent.Post event) ->
                DisplayWallInteractions.onClientTick(Minecraft.getInstance().options.keyAttack.isDown()));
    }

    @Override
    public ModBaseForge<?> getMod() {
        return ColossalChestsForge._instance;
    }

}
