package com.useandget.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.useandget.entity.Customer;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CooldownServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 21, 9, 0);

    @Mock
    private OfferRepository offerRepository;

    private CooldownService cooldownService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        cooldownService = new CooldownService(
                offerRepository,
                Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        customer = new Customer();
        customer.setCustomerId(10);
    }

    @Test
    void noPreviousOfferIsEligibleWithRetryCountZero() {
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer))
                .thenReturn(Optional.empty());

        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);

        assertThat(eligibility.eligible()).isTrue();
        assertThat(eligibility.nextRetryCount()).isZero();
        assertThat(eligibility.lastOffer()).isEmpty();
        verify(offerRepository).findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer);
    }

    @Test
    void activeCooldownBeforeExpiryIsNotEligible() {
        Offer lastOffer = offer(State.COOLDOWN, 1);
        lastOffer.setCooldownUntil(NOW.plusHours(1));
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer))
                .thenReturn(Optional.of(lastOffer));

        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);

        assertThat(eligibility.eligible()).isFalse();
        assertThat(eligibility.nextRetryCount()).isEqualTo(2);
        assertThat(eligibility.reason()).isEqualTo("Customer is still in cooldown");
        assertThat(eligibility.cooldownUntil()).contains(NOW.plusHours(1));
    }

    @Test
    void expiredCooldownIsEligibleAndIncrementsRetryCount() {
        Offer lastOffer = offer(State.COOLDOWN, 2);
        lastOffer.setCooldownUntil(NOW.minusSeconds(1));
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer))
                .thenReturn(Optional.of(lastOffer));

        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);

        assertThat(eligibility.eligible()).isTrue();
        assertThat(eligibility.nextRetryCount()).isEqualTo(3);
    }

    @Test
    void activeOfferIsNotEligible() {
        Offer lastOffer = offer(State.OFFERED, 0);
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer))
                .thenReturn(Optional.of(lastOffer));

        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);

        assertThat(eligibility.eligible()).isFalse();
        assertThat(eligibility.reason()).isEqualTo("Customer already has an active offer");
    }

    @Test
    void terminalStateIsNotEligible() {
        Offer lastOffer = offer(State.EXHAUSTED, 3);
        when(offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer))
                .thenReturn(Optional.of(lastOffer));

        CooldownEligibility eligibility = cooldownService.checkEligibility(customer);

        assertThat(eligibility.eligible()).isFalse();
        assertThat(eligibility.reason()).isEqualTo("Customer reached terminal offer state: EXHAUSTED");
    }

    private Offer offer(State state, int retryCount) {
        Offer offer = new Offer();
        offer.setState(state);
        offer.setRetryCount(retryCount);
        offer.setOfferedAt(NOW.minusHours(2));
        return offer;
    }
}
