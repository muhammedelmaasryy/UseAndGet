package com.useandget.controller;

import com.useandget.dto.FileProcessingResult;
import com.useandget.entity.Customer;
import com.useandget.entity.Offer;
import com.useandget.repository.CustomerRepository;
import com.useandget.service.FileIngestionService;
import com.useandget.service.OfferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestController.class)
class IngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileIngestionService fileIngestionService;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private OfferService offerService;

    @Test
    void ingest_returnsSummary_andCreatesOffersForIngestedCustomers() throws Exception {
        when(fileIngestionService.ingest(any(InputStream.class)))
                .thenReturn(new FileProcessingResult(1, 1, 0, 0));

        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setPhoneNumber("01001234567");
        when(customerRepository.findByPhoneNumber("01001234567")).thenReturn(Optional.of(customer));

        Offer offer = new Offer();
        offer.setOfferId(50);
        when(offerService.createOfferIfEligible(customer)).thenReturn(Optional.of(offer));

        MockMultipartFile file = new MockMultipartFile(
                "file", "customers.csv", "text/csv",
                "01001234567,Customer One,Data Power\n".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/ingest").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.processed").value(1));

        verify(offerService).createOfferIfEligible(customer);
    }

    @Test
    void ingest_returns400_whenFileIsEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/ingest").file(file))
                .andExpect(status().isBadRequest());

        verify(fileIngestionService, never()).ingest(any());
    }

    @Test
    void ingest_skipsOfferCreation_whenCustomerWasNotIngested() throws Exception {
        when(fileIngestionService.ingest(any(InputStream.class)))
                .thenReturn(new FileProcessingResult(1, 0, 1, 0));
        when(customerRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file", "customers.csv", "text/csv",
                "12345,Bad Number,Data Power\n".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/ingest").file(file))
                .andExpect(status().isOk());

        verify(offerService, never()).createOfferIfEligible(any());
    }
}
