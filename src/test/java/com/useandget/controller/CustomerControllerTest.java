package com.useandget.controller;

import com.useandget.dto.CustomerRequest;
import com.useandget.entity.Customer;
import com.useandget.entity.Segment;
import com.useandget.repository.CustomerRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private SegmentRepository segmentRepository;

    private Segment segment() {
        Segment segment = new Segment();
        segment.setSegmentId(1);
        segment.setName("Data Power");
        return segment;
    }

    @Test
    void getAll_returnsAllCustomers() throws Exception {
        when(customerRepository.findAll()).thenReturn(
                List.of(new Customer(1, "01001234567", "Customer One", segment()))
        );

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].phoneNumber").value("01001234567"));
    }

    @Test
    void create_resolvesSegment_andPersistsCustomer() throws Exception {
        when(segmentRepository.findById(1)).thenReturn(Optional.of(segment()));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setCustomerId(1);
            return customer;
        });

        CustomerRequest request = new CustomerRequest("01001234567", "Customer One", 1);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.phoneNumber").value("01001234567"));
    }

    @Test
    void create_returns400_whenPhoneNumberIsInvalid() throws Exception {
        CustomerRequest request = new CustomerRequest("12345", "Customer One", 1);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(customerRepository, never()).save(any());
    }

    @Test
    void create_returns404_whenSegmentDoesNotExist() throws Exception {
        when(segmentRepository.findById(1)).thenReturn(Optional.empty());

        CustomerRequest request = new CustomerRequest("01001234567", "Customer One", 1);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_removesCustomer_whenExists() throws Exception {
        when(customerRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerRepository).deleteById(1);
    }
}
