package com.useandget.controller;

import com.useandget.dto.SegmentRequest;
import com.useandget.entity.ConsumptionType;
import com.useandget.entity.Gift;
import com.useandget.entity.Segment;
import com.useandget.repository.GiftRepository;
import com.useandget.repository.SegmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SegmentController.class)
class SegmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SegmentRepository segmentRepository;

    @MockitoBean
    private GiftRepository giftRepository;

    private Gift gift() {
        return new Gift(1, "MB", 500);
    }

    private SegmentRequest request() {
        return new SegmentRequest("Data Power", ConsumptionType.MB, 500, 1, 7, 3);
    }

    @Test
    void getAll_returnsAllSegments() throws Exception {
        when(segmentRepository.findAll()).thenReturn(
                List.of(new Segment(1, "Data Power", ConsumptionType.MB, 500, 3, 7, gift()))
        );

        mockMvc.perform(get("/api/segments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Data Power"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(segmentRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/segments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_resolvesGift_andPersistsSegment() throws Exception {
        when(giftRepository.findById(1)).thenReturn(Optional.of(gift()));
        when(segmentRepository.save(any(Segment.class))).thenAnswer(invocation -> {
            Segment segment = invocation.getArgument(0);
            segment.setSegmentId(1);
            return segment;
        });

        mockMvc.perform(post("/api/segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.segmentId").value(1))
                .andExpect(jsonPath("$.name").value("Data Power"))
                .andExpect(jsonPath("$.threshold").value(500))
                .andExpect(jsonPath("$.cooldownDays").value(7))
                .andExpect(jsonPath("$.maxRetries").value(3));
    }

    @Test
    void create_returns404_whenGiftDoesNotExist() throws Exception {
        when(giftRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isNotFound());

        verify(segmentRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void delete_returns404_whenSegmentDoesNotExist() throws Exception {
        when(segmentRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/segments/99"))
                .andExpect(status().isNotFound());
    }
}
