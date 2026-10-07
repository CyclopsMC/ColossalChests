package org.cyclops.colossalchests2.lang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Every item has a name and a one-line explanation of its rule, shown by Cyclops Core on shift.
 * @author rubensworks
 */
public class TestLang {

    private static final Path ASSETS = Path.of("src/main/resources/assets/colossalchests2");

    private static JsonObject readLang() throws Exception {
        try (Reader reader = Files.newBufferedReader(ASSETS.resolve("lang/en_us.json"))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static List<String> getItemIds() throws Exception {
        try (Stream<Path> files = Files.list(ASSETS.resolve("models/item"))) {
            return files.map(file -> file.getFileName().toString().replace(".json", "")).sorted().toList();
        }
    }

    private static boolean isBlock(String id) {
        return Files.exists(ASSETS.resolve("blockstates/" + id + ".json"));
    }

    @Test
    public void testEveryItemHasNameAndInfo() throws Exception {
        JsonObject lang = readLang();
        List<String> ids = getItemIds();
        assertFalse(ids.isEmpty());
        for (String id : ids) {
            String key = (isBlock(id) ? "block" : "item") + ".colossalchests2." + id;
            assertTrue("No name for " + key, lang.has(key));
            assertTrue("No info for " + key, lang.has(key + ".info"));
            String info = lang.get(key + ".info").getAsString();
            assertFalse("Info of " + key + " spans several lines", info.contains("\n"));
            assertFalse("Info of " + key + " uses an em dash", info.contains("—"));
        }
    }

}
