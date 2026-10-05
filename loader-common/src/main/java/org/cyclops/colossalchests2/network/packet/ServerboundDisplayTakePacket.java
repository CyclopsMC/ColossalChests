package org.cyclops.colossalchests2.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * A left-click on a face of a Display wall, taking a stack or one item of what it shows there.
 * @author rubensworks
 */
public class ServerboundDisplayTakePacket extends PacketBase<ServerboundDisplayTakePacket> {

    public static final Type<ServerboundDisplayTakePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "display_take"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDisplayTakePacket> CODEC = getCodec(ServerboundDisplayTakePacket::new);

    private BlockPos pos;
    private Direction face;
    private boolean single;

    public ServerboundDisplayTakePacket() {
        super(TYPE);
    }

    public ServerboundDisplayTakePacket(BlockPos pos, Direction face, boolean single) {
        super(TYPE);
        this.pos = pos;
        this.face = face;
        this.single = single;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(face);
        buf.writeBoolean(single);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        face = buf.readEnum(Direction.class);
        single = buf.readBoolean();
    }

    @Override
    public void actionClient(Level level, Player player) {
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
        if (player.canInteractWithBlock(pos, 1.0)) {
            DisplayWallInteractions.getShownWall(level, pos, face).ifPresent(wall -> DisplayWallInteractions.take(player, wall, face, single));
        }
    }
}
