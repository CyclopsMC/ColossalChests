package org.cyclops.colossalchests2.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.cyclops.colossalchests2.config.UpgradeProperties;
import org.cyclops.cyclopscore.network.PacketBase;

import java.util.HashMap;
import java.util.Map;

/**
 * The server's material and upgrade tables, so clients show the same limits and material order.
 * @author rubensworks
 */
public class ClientboundChestTablesPacket extends PacketBase<ClientboundChestTablesPacket> {

    public static final Type<ClientboundChestTablesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_tables"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChestTablesPacket> CODEC = getCodec(ClientboundChestTablesPacket::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, MaterialProperties>> MATERIALS = ByteBufCodecs.map(
            HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodecWithRegistries(MaterialProperties.CODEC));
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, UpgradeProperties>> UPGRADES = ByteBufCodecs.map(
            HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodecWithRegistries(UpgradeProperties.CODEC));

    private ChestTables tables;

    public ClientboundChestTablesPacket() {
        super(TYPE);
    }

    public ClientboundChestTablesPacket(ChestTables tables) {
        super(TYPE);
        this.tables = tables;
    }

    public ChestTables getTables() {
        return tables;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        MATERIALS.encode(buf, tables.materials());
        UPGRADES.encode(buf, tables.upgrades());
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        tables = new ChestTables(MATERIALS.decode(buf), UPGRADES.decode(buf));
    }

    @Override
    public void actionClient(Level level, Player player) {
        ChestTablesLoader.set(tables);
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
    }

}
