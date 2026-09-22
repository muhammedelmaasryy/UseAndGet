package com.useandget.service;

import com.useandget.dto.EvaluationResult;
import com.useandget.dto.MatchedConsumption;
import com.useandget.entity.ConsumptionType;
import com.useandget.entity.Customer;
import com.useandget.entity.Gift;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 21, 9, 0);

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private RewardService rewardService;

    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationService(
                offerRepository,
                rewardService,
                Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
        );
    }

    private Offer offeredOffer(int retryCount, int threshold, int maxRetries, int cooldownDays) {
        Gift gift = new Gift(1, "MB", 500);
        Segment segment = new Segment(1, "Data Power", ConsumptionType.MB, threshold, maxRetries, cooldownDays, gift);
        Customer customer = new Customer(1, "01001234567", "Test Customer", segment);
        return new Offer(10, State.OFFERED, retryCount, NOW.minusDays(1), null, null, null, customer, segment);
    }

    @Test
    void consumedAtOrAboveThreshold_transitionsToRewarded_andGrantsReward() {
        Offer offer = offeredOffer(0, 500, 3, 7);

        EvaluationResult result = evaluationService.evaluate(new MatchedConsumption(offer, 600.0));

        assertThat(offer.getState()).isEqualTo(State.REWARDED);
        assertThat(offer.getRewardedAt()).isEqualTo(NOW);
        assertThat(offer.getEvaluatedAt()).isEqualTo(NOW);
        assertThat(result.getPreviousState()).isEqualTo(State.OFFERED);
        assertThat(result.getNewState()).isEqualTo(State.REWARDED);

        verify(offerRepository).save(offer);
        verify(rewardService).grantReward(offer);
    }

    @Test
    void consumedBelowThreshold_withRetriesRemaining_transitionsToCooldown() {
        Offer offer = offeredOffer(0, 500, 3, 7);

        EvaluationResult result = evaluationService.evaluate(new MatchedConsumption(offer, 200.0));

        assertThat(offer.getState()).isEqualTo(State.COOLDOWN);
        assertThat(offer.getCooldownUntil()).isEqualTo(NOW.plusDays(7));
        assertThat(result.getNewState()).isEqualTo(State.COOLDOWN);

        verify(offerRepository).save(offer);
        verify(rewardService, never()).grantReward(offer);
    }

    @Test
    void consumedBelowThreshold_withNoRetriesRemaining_transitionsToExhausted() {
        Offer offer = offeredOffer(3, 500, 3, 7);

        EvaluationResult result = evaluationService.evaluate(new MatchedConsumption(offer, 200.0));

        assertThat(offer.getState()).isEqualTo(State.EXHAUSTED);
        assertThat(offer.getCooldownUntil()).isNull();
        assertThat(result.getNewState()).isEqualTo(State.EXHAUSTED);

        verify(offerRepository).save(offer);
        verify(rewardService, never()).grantReward(offer);
    }

    @Test
    void evaluatingOfferNotInOfferedState_throwsAndDoesNotPersist() {
        Offer offer = offeredOffer(0, 500, 3, 7);
        offer.setState(State.COOLDOWN);

        assertThatThrownBy(() -> evaluationService.evaluate(new MatchedConsumption(offer, 600.0)))
                .isInstanceOf(IllegalStateException.class);

        verify(offerRepository, never()).save(offer);
        verify(rewardService, never()).grantReward(offer);
    }
}
