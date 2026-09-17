package com.useandget.dto;


public class FileProcessingResult {

    private int totalRows;
    private int processed;
    private int skipped;
    private int failed;

    public FileProcessingResult() {}

    public FileProcessingResult(int totalRows, int processed, int skipped, int failed) {
        this.totalRows = totalRows;
        this.processed = processed;
        this.skipped = skipped;
        this.failed = failed;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }
}
