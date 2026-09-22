package com.useandget.controller;

import com.useandget.dto.ConsumptionProcessingResult;
import com.useandget.dto.EvaluationResult;
import com.useandget.dto.FileProcessingResult;
import com.useandget.dto.MatchedConsumption;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import com.useandget.service.ConsumptionService;
import com.useandget.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluateController.class)
class EvaluateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsumptionService consumptionService;

    @MockitoBean
    private EvaluationService evaluationService;

    @Test
    void evaluate_returnsSummaryAndEvaluations() throws Exception {
        Offer offer = new Offer();
        offer.setOfferId(10);
        MatchedConsumption match = new MatchedConsumption(offer, 600.0);
        ConsumptionProcessingResult processingResult = new ConsumptionProcessingResult(
                new FileProcessingResult(1, 1, 0, 0), List.of(match)
        );
        when(consumptionService.process(any(InputStream.class))).thenReturn(processingResult);

        EvaluationResult evaluationResult = new EvaluationResult(10, State.OFFERED, State.REWARDED, 600.0, 500);
        when(evaluationService.evaluateAll(processingResult.getMatches())).thenReturn(List.of(evaluationResult));

        MockMultipartFile file = new MockMultipartFile(
                "file", "consumption.csv", "text/csv",
                "01001234567,600\n".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/evaluate").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.processed").value(1))
                .andExpect(jsonPath("$.evaluations[0].offerId").value(10))
                .andExpect(jsonPath("$.evaluations[0].newState").value("REWARDED"));
    }

    @Test
    void evaluate_returns400_whenFileIsEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/evaluate").file(file))
                .andExpect(status().isBadRequest());

        verify(consumptionService, never()).process(any());
    }
}
