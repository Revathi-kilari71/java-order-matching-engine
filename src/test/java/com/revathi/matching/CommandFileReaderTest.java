package com.revathi.matching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandFileReaderTest {

    @TempDir
    Path directory;

    private Path writeFile(String content) throws IOException {
        Path file = directory.resolve("commands.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void readsCommandsWithBomCommentsAndWhitespace() throws IOException {
        Path file = writeFile(
                "\uFEFF# Sample commands\n"
                        + "\n"
                        + " SUBMIT, 1, SELL, 10000, 5 \n"
                        + " CANCEL, 1 \n"
        );

        assertEquals(List.of(
                new EngineCommand.Submit(
                        new Order(1, Side.SELL, 10000, 5)),
                new EngineCommand.Cancel(1)
        ), CommandFileReader.read(file));
    }

    @Test
    void reportsPhysicalLineNumberForInvalidCommand() throws IOException {
        Path file = writeFile(
                "# Comment\n"
                        + "\n"
                        + "SUBMIT,1,SELL,10000\n"
        );

        var error = assertThrows(
                IllegalArgumentException.class,
                () -> CommandFileReader.read(file)
        );

        assertTrue(error.getMessage().contains("line 3"));
        assertTrue(error.getMessage().contains("SUBMIT requires 5 fields"));
    }

    @Test
    void rejectsInvalidValuesAndFormats() throws IOException {
        List<String> invalidLines = List.of(
                "UPDATE,1",
                "CANCEL,1,extra",
                "CANCEL,",
                "CANCEL,0",
                "SUBMIT,1,HOLD,10000,5",
                "SUBMIT,abc,BUY,10000,5",
                "SUBMIT,1,BUY,0,5",
                "SUBMIT,1,BUY,10000,-1",
                "SUBMIT,1,BUY,10000,5,"
        );

        for (String line : invalidLines) {
            Path file = writeFile(line);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> CommandFileReader.read(file),
                    "Should reject: " + line
            );
        }
    }

    @Test
    void missingFileProducesIOException() {
        Path missing = directory.resolve("missing.csv");

        assertThrows(
                IOException.class,
                () -> CommandFileReader.read(missing)
        );
    }

    @Test
    void fileCommandsProduceExpectedReplayResults() throws IOException {
        Path file = writeFile(
                "SUBMIT,1,SELL,10000,5\n"
                        + "SUBMIT,2,BUY,10000,3\n"
                        + "CANCEL,1\n"
        );

        var result = ReplayRunner.replay(CommandFileReader.read(file));

        assertEquals(
                List.of(new Trade(1, 2, 1, 10000, 3)),
                result.trades()
        );
        assertEquals(List.of(true), result.cancellationResults());
        assertEquals(0, result.activeOrderCount());
    }
}