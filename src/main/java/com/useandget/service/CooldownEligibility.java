package com.useandget.service;

import com.useandget.entity.Offer;
import java.time.LocalDateTime;
import java.util.Optional;

public record CooldownEligibility(
        boolean eligible,
        int nextRetryCount,
        Optional<Offer> lastOffer,
        String reason,
        Optional<LocalDateTime> cooldownUntil
) {

    public static CooldownEligibility eligible(int nextRetryCount, Optional<Offer> lastOffer) {
        return new CooldownEligibility(true, nextRetryCount, lastOffer, "Eligible for offer", Optional.empty());
    }

    public static CooldownEligibility ineligible(
            int nextRetryCount,
            Optional<Offer> lastOffer,
            String reason,
            Optional<LocalDateTime> cooldownUntil
    ) {
        return new CooldownEligibility(false, nextRetryCount, lastOffer, reason, cooldownUntil);
    }
}
