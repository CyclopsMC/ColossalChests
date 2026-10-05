package org.cyclops.colossalchests2.proxy;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.colossalchests2.ColossalChests;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) ->
                DisplayWallInteractions.onClientTick(Minecraft.getInstance().options.keyAttack.isDown()));
    }

    @Override
    public ModBase getMod() {
        return ColossalChests._instance;
    }

}
