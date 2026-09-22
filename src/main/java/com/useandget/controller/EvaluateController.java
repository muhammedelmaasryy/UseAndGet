package com.useandget.controller;

import com.useandget.dto.ConsumptionProcessingResult;
import com.useandget.dto.EvaluationResult;
import com.useandget.dto.FileProcessingResult;
import com.useandget.service.ConsumptionService;
import com.useandget.service.EvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/**
 * Entry point for the consumption file pipeline: match rows to active offers
 * (Team A), then evaluate each match against its segment threshold and drive
 * the offer state machine, granting a reward on success (Team B / Team C).
 */
@RestController
@RequestMapping("/api/evaluate")
public class EvaluateController {

    private final ConsumptionService consumptionService;
    private final EvaluationService evaluationService;

    public EvaluateController(ConsumptionService consumptionService, EvaluationService evaluationService) {
        this.consumptionService = consumptionService;
        this.evaluationService = evaluationService;
    }

    public record EvaluateResponse(FileProcessingResult summary, List<EvaluationResult> evaluations) {}

    @PostMapping
    public EvaluateResponse evaluate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        ConsumptionProcessingResult processingResult;
        try {
            processingResult = consumptionService.process(file.getInputStream());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file: " + e.getMessage());
        }

        List<EvaluationResult> evaluations = evaluationService.evaluateAll(processingResult.getMatches());

        return new EvaluateResponse(processingResult.getSummary(), evaluations);
    }
}
