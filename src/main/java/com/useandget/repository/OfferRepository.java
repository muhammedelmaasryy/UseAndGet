package com.useandget.repository;

import com.useandget.entity.Customer;
import com.useandget.entity.Offer;
import com.useandget.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Integer> {

    Optional<Offer> findByCustomer_PhoneNumberAndState(String phoneNumber, State state);

    Optional<Offer> findTopByCustomerOrderByOfferedAtDescOfferIdDesc(Customer customer);

    Optional<Offer> findTopByCustomerCustomerIdOrderByOfferedAtDescOfferIdDesc(Integer customerId);

    List<Offer> findByState(State state);
}
