package com.useandget.controller;

import com.useandget.dto.CustomerRequest;
import com.useandget.entity.Customer;
import com.useandget.entity.Segment;
import com.useandget.repository.CustomerRepository;
import com.useandget.repository.SegmentRepository;
import com.useandget.util.PhoneNumberValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final SegmentRepository segmentRepository;

    public CustomerController(CustomerRepository customerRepository, SegmentRepository segmentRepository) {
        this.customerRepository = customerRepository;
        this.segmentRepository = segmentRepository;
    }

    @GetMapping
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@RequestBody CustomerRequest request) {
        Customer customer = new Customer();
        applyRequest(customer, request);
        return customerRepository.save(customer);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Integer id, @RequestBody CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
        applyRequest(customer, request);
        return customerRepository.save(customer);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!customerRepository.existsById(id)) {
            throw new NoSuchElementException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }

    private void applyRequest(Customer customer, CustomerRequest request) {
        String phoneNumber = PhoneNumberValidator.normalize(request.getPhoneNumber());
        if (!PhoneNumberValidator.isValid(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format: " + request.getPhoneNumber());
        }

        Segment segment = segmentRepository.findById(request.getSegmentId())
                .orElseThrow(() -> new NoSuchElementException("Segment not found: " + request.getSegmentId()));

        customer.setPhoneNumber(phoneNumber);
        customer.setName(request.getName());
        customer.setSegment(segment);
    }
}
