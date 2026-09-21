package com.useandget.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared line-by-line CSV reader used by both the customer file and consumption
 * file pipelines. A row that fails to map is recorded as an error and parsing
 * continues with the next row rather than aborting the whole file.
 */
public final class CsvFileReader {

    private static final Set<String> HEADER_MARKERS = Set.of(
            "phonenumber", "phone_number", "phone number", "phone"
    );

    private CsvFileReader() {}

    public static <T> CsvParseOutcome<T> parse(InputStream inputStream, RowMapper<T> mapper) throws IOException {
        List<T> rows = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            boolean firstDataLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                String[] columns = splitLine(trimmedLine);

                if (firstDataLine) {
                    firstDataLine = false;
                    if (isHeaderRow(columns)) {
                        continue;
                    }
                }

                totalRows++;
                try {
                    rows.add(mapper.map(columns));
                } catch (RowParseException e) {
                    errors.add(new RowError(lineNumber, trimmedLine, e.getMessage()));
                }
            }
        }

        return new CsvParseOutcome<>(rows, totalRows, errors);
    }

    private static boolean isHeaderRow(String[] columns) {
        if (columns.length == 0) {
            return false;
        }
        return HEADER_MARKERS.contains(columns[0].trim().toLowerCase());
    }

    private static String[] splitLine(String line) {
        String[] rawColumns = line.split(",", -1);
        String[] columns = new String[rawColumns.length];
        for (int i = 0; i < rawColumns.length; i++) {
            columns[i] = rawColumns[i].trim();
        }
        return columns;
    }
}
