package com.useandget.controller;

import com.useandget.dto.SegmentRequest;
import com.useandget.entity.Gift;
import com.useandget.entity.Segment;
import com.useandget.repository.GiftRepository;
import com.useandget.repository.SegmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/segments")
public class SegmentController {

    private final SegmentRepository segmentRepository;
    private final GiftRepository giftRepository;

    public SegmentController(SegmentRepository segmentRepository, GiftRepository giftRepository) {
        this.segmentRepository = segmentRepository;
        this.giftRepository = giftRepository;
    }

    @GetMapping
    public List<Segment> getAll() {
        return segmentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Segment getById(@PathVariable Integer id) {
        return segmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Segment not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Segment create(@RequestBody SegmentRequest request) {
        Segment segment = new Segment();
        applyRequest(segment, request);
        return segmentRepository.save(segment);
    }

    @PutMapping("/{id}")
    public Segment update(@PathVariable Integer id, @RequestBody SegmentRequest request) {
        Segment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Segment not found: " + id));
        applyRequest(segment, request);
        return segmentRepository.save(segment);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!segmentRepository.existsById(id)) {
            throw new NoSuchElementException("Segment not found: " + id);
        }
        segmentRepository.deleteById(id);
    }

    private void applyRequest(Segment segment, SegmentRequest request) {
        Gift gift = giftRepository.findById(request.getGiftId())
                .orElseThrow(() -> new NoSuchElementException("Gift not found: " + request.getGiftId()));

        segment.setName(request.getName());
        segment.setConsumptionType(request.getConsumptionType());
        segment.setThreshold(request.getThreshold());
        segment.setCooldownDays(request.getCooldownDays());
        segment.setMaxRetries(request.getMaxRetries());
        segment.setGift(gift);
    }
}
