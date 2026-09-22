package com.useandget.service;

import com.useandget.dto.EvaluationResult;
import com.useandget.dto.MatchedConsumption;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares a customer's consumption against their offer's segment threshold
 * and drives the offer state machine: OFFERED -> REWARDED, or OFFERED ->
 * COOLDOWN / EXHAUSTED depending on remaining retries.
 */
@Service
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);

    private final OfferRepository offerRepository;
    private final RewardService rewardService;
    private final Clock clock;

    public EvaluationService(OfferRepository offerRepository, RewardService rewardService, Clock clock) {
        this.offerRepository = offerRepository;
        this.rewardService = rewardService;
        this.clock = clock;
    }

    @Transactional
    public EvaluationResult evaluate(MatchedConsumption match) {
        Offer offer = match.getOffer();
        double consumedAmount = match.getConsumedAmount();

        if (offer.getState() != State.OFFERED) {
            throw new IllegalStateException(
                    "Cannot evaluate offer " + offer.getOfferId()
                            + " in state " + offer.getState() + ", expected OFFERED"
            );
        }

        State previousState = offer.getState();
        Segment segment = offer.getSegment();
        int threshold = segment.getThreshold();
        LocalDateTime now = LocalDateTime.now(clock);

        offer.setEvaluatedAt(now);

        if (consumedAmount >= threshold) {
            offer.setState(State.REWARDED);
            offer.setRewardedAt(now);
        } else if (offer.getRetryCount() < segment.getMaxRetries()) {
            offer.setState(State.COOLDOWN);
            offer.setCooldownUntil(now.plusDays(segment.getCooldownDays()));
        } else {
            offer.setState(State.EXHAUSTED);
        }

        offerRepository.save(offer);

        log.info("Evaluated offer {}: {} -> {} (consumed={}, threshold={})",
                offer.getOfferId(), previousState, offer.getState(), consumedAmount, threshold);

        if (offer.getState() == State.REWARDED) {
            rewardService.grantReward(offer);
        }

        return new EvaluationResult(offer.getOfferId(), previousState, offer.getState(), consumedAmount, threshold);
    }

    public List<EvaluationResult> evaluateAll(List<MatchedConsumption> matches) {
        List<EvaluationResult> results = new ArrayList<>();
        for (MatchedConsumption match : matches) {
            results.add(evaluate(match));
        }
        return results;
    }
}
