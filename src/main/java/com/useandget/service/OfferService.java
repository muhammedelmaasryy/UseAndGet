package com.useandget.service;

import com.useandget.entity.Customer;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import com.useandget.repository.CustomerRepository;
import com.useandget.repository.OfferRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferService {

    private final CustomerRepository customerRepository;
    private final OfferRepository offerRepository;
    private final CooldownService cooldownService;
    private final Clock clock;

    public OfferService(
            CustomerRepository customerRepository,
            OfferRepository offerRepository,
            CooldownService cooldownService,
            Clock clock
    ) {
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
        this.cooldownService = cooldownService;
        this.clock = clock;
    }

    @Transactional
    public Offer createOffer(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        return createOffer(customer);
    }

    @Transactional
    public Offer createOffer(Customer customer) {
        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);
        if (!eligibility.eligible()) {
            throw new OfferNotEligibleException(eligibility.reason());
        }

        return offerRepository.save(buildOffer(customer, eligibility));
    }

    @Transactional
    public Optional<Offer> createOfferIfEligible(Customer customer) {
        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);
        if (!eligibility.eligible()) {
            return Optional.empty();
        }

        return Optional.of(offerRepository.save(buildOffer(customer, eligibility)));
    }

    private Offer buildOffer(Customer customer, CooldownEligibility eligibility) {
        Offer offer = new Offer();
        offer.setCustomer(customer);
        offer.setSegment(customer.getSegment());
        offer.setState(State.OFFERED);
        offer.setRetryCount(eligibility.nextRetryCount());
        offer.setOfferedAt(LocalDateTime.now(clock));
        return offer;
    }
}
