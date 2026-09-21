package com.useandget.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvFileReaderTest {

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parse_skipsHeaderRow_whenPresent() throws IOException {
        String csv = "phoneNumber,name,segmentName\n01001234567,Test,Data Power\n";

        CsvParseOutcome<String> outcome = CsvFileReader.parse(stream(csv), columns -> columns[0]);

        assertEquals(1, outcome.getTotalRows());
        assertEquals(1, outcome.getRows().size());
        assertEquals("01001234567", outcome.getRows().get(0));
    }

    @Test
    void parse_treatsFirstRowAsData_whenNoHeaderPresent() throws IOException {
        String csv = "01001234567,Test,Data Power\n";

        CsvParseOutcome<String> outcome = CsvFileReader.parse(stream(csv), columns -> columns[0]);

        assertEquals(1, outcome.getTotalRows());
        assertEquals(1, outcome.getRows().size());
    }

    @Test
    void parse_skipsBlankLines() throws IOException {
        String csv = "01001234567,Test,Data Power\n\n\n01009876543,Test2,Data Power\n";

        CsvParseOutcome<String> outcome = CsvFileReader.parse(stream(csv), columns -> columns[0]);

        assertEquals(2, outcome.getTotalRows());
    }

    @Test
    void parse_collectsRowErrors_withoutStoppingOtherRows() throws IOException {
        String csv = "01001234567,Test,Data Power\nBADROW\n01009876543,Test2,Data Power\n";

        CsvParseOutcome<String> outcome = CsvFileReader.parse(stream(csv), columns -> {
            if (columns.length < 3) {
                throw new RowParseException("not enough columns");
            }
            return columns[0];
        });

        assertEquals(3, outcome.getTotalRows());
        assertEquals(2, outcome.getRows().size());
        assertEquals(1, outcome.getErrors().size());
        assertTrue(outcome.getErrors().get(0).getReason().contains("not enough columns"));
    }
}
