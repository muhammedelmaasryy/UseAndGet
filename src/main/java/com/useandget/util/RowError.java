package com.useandget.util;

public class RowError {

    private final int lineNumber;
    private final String rawLine;
    private final String reason;

    public RowError(int lineNumber, String rawLine, String reason) {
        this.lineNumber = lineNumber;
        this.rawLine = rawLine;
        this.reason = reason;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getReason() {
        return reason;
    }
}
