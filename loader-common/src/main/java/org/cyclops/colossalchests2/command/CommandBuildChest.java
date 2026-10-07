package org.cyclops.colossalchests2.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.multiblock.ChestBuilder;
import org.cyclops.colossalchests2.multiblock.ChestStructure;

import java.util.List;
import java.util.Optional;

/**
 * Builds a complete chest: /colossalchests2 build &lt;material&gt; &lt;size&gt; [&lt;pos&gt;] [replace]
 * Without a position, the chest is placed in front of the source.
 * @author rubensworks
 */
public final class CommandBuildChest {

    private static final String PREFIX = "command.colossalchests2.build.";
    private static final DynamicCommandExceptionType UNKNOWN_MATERIAL = new DynamicCommandExceptionType(
            name -> Component.translatable(PREFIX + "unknown_material", name));
    private static final SimpleCommandExceptionType NOT_LOADED = new SimpleCommandExceptionType(
            Component.translatable(PREFIX + "not_loaded"));
    private static final SimpleCommandExceptionType NOT_FORMED = new SimpleCommandExceptionType(
            Component.translatable(PREFIX + "not_formed"));

    private CommandBuildChest() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("build")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("material", ResourceLocationArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                ChestMaterial.getAll().stream().map(ChestMaterial::id), builder))
                        .then(withReplace(Commands.argument("size", IntegerArgumentType.integer(GeneralConfig.MIN_SIZE, GeneralConfig.HARD_MAX_SIZE)),
                                (context, replace) -> run(context, null, replace))
                                .then(withReplace(Commands.argument("pos", BlockPosArgument.blockPos()),
                                        (context, replace) -> run(context, BlockPosArgument.getBlockPos(context, "pos"), replace)))));
    }

    /**
     * @param id A material id, where a name without namespace, such as "iron", matches the material with that name.
     */
    private static ChestMaterial getMaterial(ResourceLocation id) throws CommandSyntaxException {
        Optional<ChestMaterial> material = ChestMaterial.byId(id);
        if (material.isEmpty() && id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            List<ChestMaterial> named = ChestMaterial.getAll().stream().filter(candidate -> candidate.getName().equals(id.getPath())).toList();
            material = named.size() == 1 ? Optional.of(named.getFirst()) : Optional.empty();
        }
        return material.orElseThrow(() -> UNKNOWN_MATERIAL.create(id.toString()));
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T withReplace(T builder, Action action) {
        return builder
                .executes(context -> action.run(context, false))
                .then(Commands.literal("replace").executes(context -> action.run(context, true)));
    }

    private static int run(CommandContext<CommandSourceStack> context, BlockPos min, boolean replace) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ChestMaterial material = getMaterial(ResourceLocationArgument.getId(context, "material"));
        int size = IntegerArgumentType.getInteger(context, "size");
        int maxSize = material.getProperties().maxSize();
        if (size > maxSize) {
            source.sendFailure(Component.translatable(PREFIX + "too_large", material.getDisplayName(), maxSize));
            return 0;
        }

        ServerLevel level = source.getLevel();
        ChestStructure structure = min != null ? new ChestStructure(min, size)
                : ChestBuilder.inFront(BlockPos.containing(source.getPosition()), Direction.fromYRot(source.getRotation().y), size);
        if (!ChestBuilder.isLoaded(level, structure)) {
            throw NOT_LOADED.create();
        }
        if (!replace) {
            List<BlockPos> obstructions = ChestBuilder.findObstructions(level, structure);
            if (!obstructions.isEmpty()) {
                BlockPos first = obstructions.getFirst();
                source.sendFailure(Component.translatable(PREFIX + "obstructed", obstructions.size(), first.getX(), first.getY(), first.getZ()));
                return 0;
            }
        }

        BlockPos corePos = ChestBuilder.getCorePos(structure, source.getPosition());
        if (ChestBuilder.build(level, structure, material, corePos).isEmpty()) {
            throw NOT_FORMED.create();
        }
        source.sendSuccess(() -> Component.translatable(PREFIX + "success", size, material.getDisplayName(),
                corePos.getX(), corePos.getY(), corePos.getZ()), true);
        return 1;
    }

    private interface Action {
        int run(CommandContext<CommandSourceStack> context, boolean replace) throws CommandSyntaxException;
    }

}
