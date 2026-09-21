package com.useandget.util;

import java.util.Collections;
import java.util.List;

public class CsvParseOutcome<T> {

    private final List<T> rows;
    private final int totalRows;
    private final List<RowError> errors;

    public CsvParseOutcome(List<T> rows, int totalRows, List<RowError> errors) {
        this.rows = Collections.unmodifiableList(rows);
        this.totalRows = totalRows;
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<T> getRows() {
        return rows;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public List<RowError> getErrors() {
        return errors;
    }
}
