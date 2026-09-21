package com.useandget.service;

import com.useandget.dto.FileProcessingResult;
import com.useandget.entity.Customer;
import com.useandget.entity.Segment;
import com.useandget.repository.CustomerRepository;
import com.useandget.repository.SegmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FileIngestionServiceTest {

    private CustomerRepository customerRepository;
    private SegmentRepository segmentRepository;
    private FileIngestionService fileIngestionService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        segmentRepository = mock(SegmentRepository.class);
        fileIngestionService = new FileIngestionService(customerRepository, segmentRepository);
    }

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private Segment dataPowerSegment() {
        return new Segment(1, "Data Power", null, 500, 3, 7, null);
    }

    @Test
    void ingest_createsNewCustomer_whenPhoneNumberIsValidAndSegmentExists() throws IOException {
        when(segmentRepository.findByNameIgnoreCase("Data Power")).thenReturn(Optional.of(dataPowerSegment()));
        when(customerRepository.findByPhoneNumber("01001234567")).thenReturn(Optional.empty());

        String csv = "01001234567,Test Customer,Data Power\n";
        FileProcessingResult result = fileIngestionService.ingest(stream(csv));

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getProcessed());
        assertEquals(0, result.getSkipped());
        assertEquals(0, result.getFailed());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void ingest_updatesExistingCustomer_whenPhoneNumberAlreadyExists() throws IOException {
        Segment segment = dataPowerSegment();
        Customer existing = new Customer(5, "01001234567", "Old Name", segment);

        when(segmentRepository.findByNameIgnoreCase("Data Power")).thenReturn(Optional.of(segment));
        when(customerRepository.findByPhoneNumber("01001234567")).thenReturn(Optional.of(existing));

        String csv = "01001234567,New Name,Data Power\n";
        FileProcessingResult result = fileIngestionService.ingest(stream(csv));

        assertEquals(1, result.getProcessed());
        verify(customerRepository).save(existing);
        assertEquals("New Name", existing.getName());
    }

    @Test
    void ingest_skipsRow_whenPhoneNumberFormatIsInvalid() throws IOException {
        String csv = "12345,Test Customer,Data Power\n";
        FileProcessingResult result = fileIngestionService.ingest(stream(csv));

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getProcessed());
        assertEquals(1, result.getSkipped());
        verifyNoInteractions(customerRepository);
    }

    @Test
    void ingest_skipsDuplicatePhoneNumbersWithinSameFile() throws IOException {
        when(segmentRepository.findByNameIgnoreCase("Data Power")).thenReturn(Optional.of(dataPowerSegment()));
        when(customerRepository.findByPhoneNumber("01001234567")).thenReturn(Optional.empty());

        String csv = "01001234567,Test Customer,Data Power\n01001234567,Duplicate,Data Power\n";
        FileProcessingResult result = fileIngestionService.ingest(stream(csv));

        assertEquals(2, result.getTotalRows());
        assertEquals(1, result.getProcessed());
        assertEquals(1, result.getSkipped());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void ingest_skipsRow_whenSegmentDoesNotExist() throws IOException {
        when(segmentRepository.findByNameIgnoreCase("Unknown")).thenReturn(Optional.empty());

        String csv = "01001234567,Test Customer,Unknown\n";
        FileProcessingResult result = fileIngestionService.ingest(stream(csv));

        assertEquals(1, result.getSkipped());
        verifyNoInteractions(customerRepository);
    }

    @Test
    void ingest_countsMalformedRows_asFailed() throws IOException {
        String csv = "01001234567,OnlyTwoColumns\n";
        FileProcessingResult result = fileIngestionService.ingest(stream(csv));

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getFailed());
        assertEquals(0, result.getProcessed());
    }
}
