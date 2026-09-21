package com.useandget.service;

import com.useandget.entity.Customer;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CooldownService {

    private final OfferRepository offerRepository;
    private final Clock clock;

    public CooldownService(OfferRepository offerRepository, Clock clock) {
        this.offerRepository = offerRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CooldownEligibility checkEligibility(Customer customer) {
        Optional<Offer> lastOffer = offerRepository.findTopByCustomerOrderByOfferedAtDescOfferIdDesc(customer);
        if (lastOffer.isEmpty()) {
            return CooldownEligibility.eligible(0, Optional.empty());
        }

        Offer offer = lastOffer.get();
        int nextRetryCount = nextRetryCount(offer);

        if (offer.getState() == State.COOLDOWN) {
            return evaluateCooldown(offer, nextRetryCount, lastOffer);
        }

        if (offer.getState() == State.OFFERED) {
            return CooldownEligibility.ineligible(
                    nextRetryCount,
                    lastOffer,
                    "Customer already has an active offer",
                    Optional.empty()
            );
        }

        return CooldownEligibility.ineligible(
                nextRetryCount,
                lastOffer,
                "Customer reached terminal offer state: " + offer.getState(),
                Optional.empty()
        );
    }

    private CooldownEligibility evaluateCooldown(
            Offer offer,
            int nextRetryCount,
            Optional<Offer> lastOffer
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cooldownUntil = offer.getCooldownUntil();

        if (cooldownUntil != null && cooldownUntil.isAfter(now)) {
            return CooldownEligibility.ineligible(
                    nextRetryCount,
                    lastOffer,
                    "Customer is still in cooldown",
                    Optional.of(cooldownUntil)
            );
        }

        return CooldownEligibility.eligible(nextRetryCount, lastOffer);
    }

    private int nextRetryCount(Offer offer) {
        return offer.getRetryCount() == null ? 1 : offer.getRetryCount() + 1;
    }
}
