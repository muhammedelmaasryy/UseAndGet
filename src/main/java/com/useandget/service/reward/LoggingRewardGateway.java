package com.useandget.service.reward;

import com.useandget.dto.RewardResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingRewardGateway implements RewardGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingRewardGateway.class);

    @Override
    public void issue(RewardResult result) {
        log.info(
                "REWARD ISSUED -> offerId={}, phoneNumber={}, giftType={}, giftAmount={}",
                result.getOfferId(),
                result.getPhoneNumber(),
                result.getGiftType(),
                result.getGiftAmount()
        );
    }
}