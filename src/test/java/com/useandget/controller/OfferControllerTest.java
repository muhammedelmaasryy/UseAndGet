package com.useandget.controller;

import com.useandget.entity.ConsumptionType;
import com.useandget.entity.Customer;
import com.useandget.entity.Gift;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfferController.class)
class OfferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfferRepository offerRepository;

    private Offer offer(State state) {
        Gift gift = new Gift(1, "MB", 500);
        Segment segment = new Segment(1, "Data Power", ConsumptionType.MB, 500, 3, 7, gift);
        Customer customer = new Customer(1, "01001234567", "Test Customer", segment);
        return new Offer(10, state, 0, LocalDateTime.now(), null, null, null, customer, segment);
    }

    @Test
    void getOffers_withoutStateFilter_returnsAllOffers() throws Exception {
        when(offerRepository.findAll()).thenReturn(List.of(offer(State.OFFERED)));

        mockMvc.perform(get("/api/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].offerId").value(10))
                .andExpect(jsonPath("$[0].phoneNumber").value("01001234567"))
                .andExpect(jsonPath("$[0].segmentName").value("Data Power"))
                .andExpect(jsonPath("$[0].state").value("OFFERED"));

        verify(offerRepository, never()).findByState(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getOffers_withStateFilter_delegatesToRepository() throws Exception {
        when(offerRepository.findByState(State.REWARDED)).thenReturn(List.of(offer(State.REWARDED)));

        mockMvc.perform(get("/api/offers").param("state", "REWARDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].state").value("REWARDED"));

        verify(offerRepository).findByState(State.REWARDED);
        verify(offerRepository, never()).findAll();
    }
}
