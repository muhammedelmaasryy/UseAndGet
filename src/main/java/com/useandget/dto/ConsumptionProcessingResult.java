package com.useandget.dto;

import java.util.Collections;
import java.util.List;

/**
 * Full result of processing a consumption file: summary counts for the API
 * response, plus the rows that matched an active offer and are ready for
 * EvaluationService.
 */
public class ConsumptionProcessingResult {

    private FileProcessingResult summary;
    private List<MatchedConsumption> matches;

    public ConsumptionProcessingResult() {}

    public ConsumptionProcessingResult(FileProcessingResult summary, List<MatchedConsumption> matches) {
        this.summary = summary;
        this.matches = Collections.unmodifiableList(matches);
    }

    public FileProcessingResult getSummary() {
        return summary;
    }

    public void setSummary(FileProcessingResult summary) {
        this.summary = summary;
    }

    public List<MatchedConsumption> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchedConsumption> matches) {
        this.matches = matches;
    }
}
