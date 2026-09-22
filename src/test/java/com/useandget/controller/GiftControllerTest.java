package com.useandget.controller;

import tools.jackson.databind.ObjectMapper;
import com.useandget.dto.GiftRequest;
import com.useandget.entity.Gift;
import com.useandget.repository.GiftRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GiftController.class)
class GiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GiftRepository giftRepository;

    @Test
    void getAll_returnsAllGifts() throws Exception {
        when(giftRepository.findAll()).thenReturn(List.of(new Gift(1, "MB", 500)));

        mockMvc.perform(get("/api/gifts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].giftId").value(1))
                .andExpect(jsonPath("$[0].giftType").value("MB"))
                .andExpect(jsonPath("$[0].giftAmount").value(500));
    }

    @Test
    void getById_returnsGift_whenFound() throws Exception {
        when(giftRepository.findById(1)).thenReturn(Optional.of(new Gift(1, "MB", 500)));

        mockMvc.perform(get("/api/gifts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.giftType").value("MB"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(giftRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gifts/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_persistsAndReturns201() throws Exception {
        GiftRequest request = new GiftRequest("SMS", 100);
        when(giftRepository.save(any(Gift.class))).thenAnswer(invocation -> {
            Gift gift = invocation.getArgument(0);
            gift.setGiftId(5);
            return gift;
        });

        mockMvc.perform(post("/api/gifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.giftId").value(5))
                .andExpect(jsonPath("$.giftType").value("SMS"));
    }

    @Test
    void update_returns404_whenGiftDoesNotExist() throws Exception {
        when(giftRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/gifts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRequest("MB", 500))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_removesGift_whenExists() throws Exception {
        when(giftRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/gifts/1"))
                .andExpect(status().isNoContent());

        verify(giftRepository).deleteById(1);
    }

    @Test
    void delete_returns404_whenGiftDoesNotExist() throws Exception {
        when(giftRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/gifts/99"))
                .andExpect(status().isNotFound());

        verify(giftRepository, never()).deleteById(any());
    }
}
