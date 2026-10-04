package org.cyclops.colossalchests2.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * A left-click on a Display wall, taking a stack or one item of what it shows.
 * @author rubensworks
 */
public class ServerboundDisplayTakePacket extends PacketBase<ServerboundDisplayTakePacket> {

    public static final Type<ServerboundDisplayTakePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "display_take"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDisplayTakePacket> CODEC = getCodec(ServerboundDisplayTakePacket::new);

    private BlockPos pos;
    private boolean single;

    public ServerboundDisplayTakePacket() {
        super(TYPE);
    }

    public ServerboundDisplayTakePacket(BlockPos pos, boolean single) {
        super(TYPE);
        this.pos = pos;
        this.single = single;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(single);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        single = buf.readBoolean();
    }

    @Override
    public void actionClient(Level level, Player player) {
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
        if (player.canInteractWithBlock(pos, 1.0) && DisplayWallInteractions.isDisplayWall(level.getBlockState(pos))
                && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
            DisplayWallInteractions.take(player, wall, single);
        }
    }
}
