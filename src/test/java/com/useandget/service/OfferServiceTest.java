package com.useandget.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.useandget.entity.Customer;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.repository.CustomerRepository;
import com.useandget.repository.OfferRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 21, 9, 0);

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CooldownService cooldownService;

    private OfferService offerService;
    private Customer customer;
    private Segment segment;

    @BeforeEach
    void setUp() {
        offerService = new OfferService(
                customerRepository,
                offerRepository,
                cooldownService,
                Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
        segment = new Segment();
        segment.setSegmentId(5);
        customer = new Customer();
        customer.setCustomerId(10);
        customer.setSegment(segment);
    }

    @Test
    void createOfferPersistsOfferedStateWithRetryCountFromEligibility() {
        when(customerRepository.findById(10)).thenReturn(Optional.of(customer));
        when(cooldownService.checkEligibility(customer))
                .thenReturn(CooldownEligibility.eligible(2, Optional.empty()));
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Offer offer = offerService.createOffer(10);

        assertThat(offer.getCustomer()).isSameAs(customer);
        assertThat(offer.getSegment()).isSameAs(segment);
        assertThat(offer.getState()).isEqualTo(State.OFFERED);
        assertThat(offer.getRetryCount()).isEqualTo(2);
        assertThat(offer.getOfferedAt()).isEqualTo(NOW);

        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(offer);
    }

    @Test
    void createOfferThrowsWhenCustomerIsNotEligible() {
        when(cooldownService.checkEligibility(customer))
                .thenReturn(CooldownEligibility.ineligible(
                        1,
                        Optional.empty(),
                        "Customer is still in cooldown",
                        Optional.empty()
                ));

        assertThatThrownBy(() -> offerService.createOffer(customer))
                .isInstanceOf(OfferNotEligibleException.class)
                .hasMessage("Customer is still in cooldown");

        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void createOfferIfEligibleReturnsEmptyWithoutPersistingWhenSkipped() {
        when(cooldownService.checkEligibility(customer))
                .thenReturn(CooldownEligibility.ineligible(
                        1,
                        Optional.empty(),
                        "Customer already has an active offer",
                        Optional.empty()
                ));

        Optional<Offer> offer = offerService.createOfferIfEligible(customer);

        assertThat(offer).isEmpty();
        verify(offerRepository, never()).save(any(Offer.class));
    }
}
