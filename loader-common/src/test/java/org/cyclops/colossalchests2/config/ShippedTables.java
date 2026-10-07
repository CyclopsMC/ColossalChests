package org.cyclops.colossalchests2.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The tables from this mod's shipped data files, for tests that depend on their values.
 * @author rubensworks
 */
public final class ShippedTables {

    private ShippedTables() {
    }

    public static ChestTables load() {
        Map<ResourceLocation, JsonElement> files = Maps.newHashMap();
        try {
            Path root = Path.of(Objects.requireNonNull(ShippedTables.class.getResource("/data/" + Reference.MOD_ID + "/" + ChestTablesLoader.DIRECTORY)).toURI());
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                    String name = root.relativize(path).toString().replace('\\', '/');
                    try (Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
                        files.put(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, name.substring(0, name.length() - ".json".length())),
                                JsonParser.parseReader(reader));
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return ChestTablesLoader.fromJson(files);
    }

}
