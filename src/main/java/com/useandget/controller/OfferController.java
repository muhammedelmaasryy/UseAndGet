package com.useandget.controller;

import com.useandget.dto.OfferResponse;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import com.useandget.repository.OfferRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferRepository offerRepository;

    public OfferController(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @GetMapping
    public List<OfferResponse> getOffers(@RequestParam(required = false) State state) {
        List<Offer> offers = state != null ? offerRepository.findByState(state) : offerRepository.findAll();
        return offers.stream().map(OfferController::toResponse).toList();
    }

    private static OfferResponse toResponse(Offer offer) {
        return new OfferResponse(
                offer.getOfferId(),
                offer.getCustomer() != null ? offer.getCustomer().getPhoneNumber() : null,
                offer.getSegment() != null ? offer.getSegment().getName() : null,
                offer.getState(),
                offer.getRetryCount(),
                offer.getOfferedAt(),
                offer.getEvaluatedAt(),
                offer.getCooldownUntil(),
                offer.getRewardedAt()
        );
    }
}
