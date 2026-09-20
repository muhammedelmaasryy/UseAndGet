package com.useandget.service;

import com.useandget.dto.RewardResult;
import com.useandget.entity.Customer;
import com.useandget.entity.Gift;
import com.useandget.entity.Offer;
import com.useandget.entity.Segment;
import com.useandget.entity.State;
import com.useandget.service.reward.RewardGateway;
import org.springframework.stereotype.Service;

@Service
public class RewardService {

    private final RewardGateway rewardGateway;

    public RewardService(RewardGateway rewardGateway) {
        this.rewardGateway = rewardGateway;
    }

    public RewardResult grantReward(Offer offer) {
        if (offer == null) {
            throw new IllegalArgumentException("Offer must not be null");
        }
        if (offer.getState() != State.REWARDED) {
            throw new IllegalStateException(
                    "grantReward called on offer " + offer.getOfferId()
                            + " but state is " + offer.getState() + ", expected REWARDED"
            );
        }

        Segment segment = offer.getSegment();
        if (segment == null) {
            throw new IllegalStateException("Offer " + offer.getOfferId() + " has no segment");
        }

        Gift gift = segment.getGift();
        if (gift == null) {
            throw new IllegalStateException(
                    "Segment " + segment.getSegmentId() + " has no gift configured"
            );
        }

        Customer customer = offer.getCustomer();
        String phoneNumber = customer != null ? customer.getPhoneNumber() : null;

        RewardResult result = new RewardResult(
                offer.getOfferId(),
                phoneNumber,
                gift.getGiftType(),
                gift.getGiftAmount(),
                "Reward granted: " + gift.getGiftAmount() + " " + gift.getGiftType()
        );

        rewardGateway.issue(result);

        return result;
    }
}