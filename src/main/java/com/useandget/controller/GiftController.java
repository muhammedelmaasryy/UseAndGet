package com.useandget.controller;

import com.useandget.dto.GiftRequest;
import com.useandget.entity.Gift;
import com.useandget.repository.GiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/gifts")
public class GiftController {

    private final GiftRepository giftRepository;

    public GiftController(GiftRepository giftRepository) {
        this.giftRepository = giftRepository;
    }

    @GetMapping
    public List<Gift> getAll() {
        return giftRepository.findAll();
    }

    @GetMapping("/{id}")
    public Gift getById(@PathVariable Integer id) {
        return giftRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Gift not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Gift create(@RequestBody GiftRequest request) {
        Gift gift = new Gift();
        gift.setGiftType(request.getGiftType());
        gift.setGiftAmount(request.getGiftAmount());
        return giftRepository.save(gift);
    }

    @PutMapping("/{id}")
    public Gift update(@PathVariable Integer id, @RequestBody GiftRequest request) {
        Gift gift = giftRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Gift not found: " + id));
        gift.setGiftType(request.getGiftType());
        gift.setGiftAmount(request.getGiftAmount());
        return giftRepository.save(gift);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!giftRepository.existsById(id)) {
            throw new NoSuchElementException("Gift not found: " + id);
        }
        giftRepository.deleteById(id);
    }
}
