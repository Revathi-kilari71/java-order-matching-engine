package com.revathi.matching;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CommandFileReader {

    private CommandFileReader() {
    }

    public static List<EngineCommand> read(Path path)
            throws IOException {

        List<EngineCommand> commands = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Allow files saved with a UTF-8 byte order mark.
                if (lineNumber == 1 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                line = line.strip();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    commands.add(parse(line));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "Invalid command at line " + lineNumber
                                    + ": " + exception.getMessage(),
                            exception
                    );
                }
            }
        }

        return List.copyOf(commands);
    }

    private static EngineCommand parse(String line) {
        String[] fields = line.split(",", -1);

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].strip();
        }

        return switch (fields[0]) {
            case "SUBMIT" -> {
                if (fields.length != 5) {
                    throw new IllegalArgumentException(
                            "SUBMIT requires 5 fields"
                    );
                }

                yield new EngineCommand.Submit(new Order(
                        Long.parseLong(fields[1]),
                        Side.valueOf(fields[2]),
                        Long.parseLong(fields[3]),
                        Long.parseLong(fields[4])
                ));
            }

            case "CANCEL" -> {
                if (fields.length != 2) {
                    throw new IllegalArgumentException(
                            "CANCEL requires 2 fields"
                    );
                }

                yield new EngineCommand.Cancel(
                        Long.parseLong(fields[1])
                );
            }

            default -> throw new IllegalArgumentException(
                    "Unknown command: " + fields[0]
            );
        };
    }
}