package org.example;

import trees.TreeOperationResponse;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The example files must match what the code produces now. Run Main to regenerate them. */
class ExamplesTest {

    @Test
    void insertExampleIsUpToDate() throws Exception {
        assertMatches("example-insert.json", Main.insertExample());
    }

    @Test
    void deleteExampleIsUpToDate() throws Exception {
        assertMatches("example-delete.json", Main.deleteExample());
    }

    private static void assertMatches(String file, TreeOperationResponse expected) throws Exception {
        String actual = Files.readString(Path.of(file));
        assertEquals(normalize(expected.toJson()), normalize(actual), file + " is outdated, run Main");
    }

    private static String normalize(String json) {
        return json.replace("\r\n", "\n").strip();
    }
}
