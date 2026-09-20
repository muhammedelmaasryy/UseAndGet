package com.useandget.service;

import com.useandget.dto.RewardResult;
import com.useandget.entity.Customer;
import com.useandget.entity.Gift;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.service.reward.RewardGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RewardServiceTest {

    private RewardGateway rewardGateway;
    private RewardService rewardService;

    @BeforeEach
    void setUp() {
        rewardGateway = mock(RewardGateway.class);
        rewardService = new RewardService(rewardGateway);
    }

    private Offer rewardedOffer() {
        Gift gift = new Gift(1, "MB", 500);
        Segment segment = new Segment(
                1, "Data Power", null, 500, 3, 7, gift
        );
        Customer customer = new Customer(1, "01001234567", "Test Customer", segment);
        return new Offer(
                10, State.REWARDED, 0, null, null, null, null, customer, segment
        );
    }

    @Test
    void grantReward_buildsResultFromSegmentGift_andCallsGateway() {
        Offer offer = rewardedOffer();

        RewardResult result = rewardService.grantReward(offer);

        assertEquals(10, result.getOfferId());
        assertEquals("01001234567", result.getPhoneNumber());
        assertEquals("MB", result.getGiftType());
        assertEquals(500, result.getGiftAmount());

        ArgumentCaptor<RewardResult> captor = ArgumentCaptor.forClass(RewardResult.class);
        verify(rewardGateway).issue(captor.capture());
        assertEquals(result.getOfferId(), captor.getValue().getOfferId());
    }

    @Test
    void grantReward_rejectsOfferNotInRewardedState() {
        Offer offer = rewardedOffer();
        offer.setState(State.COOLDOWN);

        assertThrows(IllegalStateException.class, () -> rewardService.grantReward(offer));
    }

    @Test
    void grantReward_rejectsNullOffer() {
        assertThrows(IllegalArgumentException.class, () -> rewardService.grantReward(null));
    }
}