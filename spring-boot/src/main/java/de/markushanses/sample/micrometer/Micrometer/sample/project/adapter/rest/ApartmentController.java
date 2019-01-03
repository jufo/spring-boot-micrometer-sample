package de.markushanses.sample.micrometer.Micrometer.sample.project.adapter.rest;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "api/apartment")
public class ApartmentController {

    private final Counter rentSuccessCounter;
    private final Counter startRentCounter;
    private final Counter cancelCounter;
    private final AtomicInteger rentProgress;
    private final Timer rentTimer;
    private final Random random;

    @Autowired
    public ApartmentController(MeterRegistry registry) {
        this.startRentCounter = registry.counter("counter.apartment.rent.start", "object_type", "apartment", "rent_process_state", "started");
        this.rentSuccessCounter = registry.counter("counter.apartment.rent.success", "object_type", "apartment", "rent_process_state", "success");
        this.cancelCounter = registry.counter("counter.apartment.rent.cancel", "object_type", "apartment", "rent_process_state", "aborted");
        this.rentProgress = registry.gauge("gauge.apartment.rent.progress", Tags.of("object_type", "apartment", "rent_process_state", "ongoing"), new AtomicInteger(0));
        this.rentTimer = Timer.builder("timer.apartment.rent").publishPercentileHistogram().register(registry);
        this.random = new Random();
    }

    @PostMapping(path = "/reserve-apartment", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity reserveApartment(@RequestBody ReserveApartmentRequest request) {
        if (request.getApartmentId() == 2l) {
            throw new ApartmentNotFoundException("No apartment found");
        }
        startRentCounter.increment();
        rentProgress.incrementAndGet();
        return new ResponseEntity("Reserved apartment with ID: " + request.getApartmentId(), HttpStatus.CREATED);
    }

    @GetMapping("/confirm-apartment-rent/{apartmentNumber}")
    public ResponseEntity<String> rentApartment(@PathVariable Long apartmentNumber) {
        if (apartmentNumber == 2l) {
            throw new ApartmentNotFoundException("No apartment found");
        }
        rentProgress.decrementAndGet();
        rentSuccessCounter.increment();
        return new ResponseEntity("Apartment rent ID: " + Clock.systemDefaultZone().millis(), HttpStatus.OK);
    }


    @GetMapping("/cancel-apartment-rent/{apartmentNumber}")
    public ResponseEntity<String> cancelRentApartment(@PathVariable Long apartmentNumber) {
        if (apartmentNumber == 2l) {
            throw new ApartmentNotFoundException("No apartment found");
        }
        cancelCounter.increment();
        rentProgress.decrementAndGet();
        return new ResponseEntity("Apartment rent aborted ID: " + Clock.systemDefaultZone().millis(), HttpStatus.OK);
    }

    @GetMapping("/delay/{delayInSeconds}")
    public ResponseEntity<String> cancelRentApartmentWithDelay(@PathVariable Long delayInSeconds) {
        rentTimer.record(simulatedLatency(delayInSeconds), TimeUnit.SECONDS);
        return new ResponseEntity("Apartment delay ID: " + Clock.systemDefaultZone().millis(), HttpStatus.OK);
    }

    private long simulatedLatency(long center) {
        return (long) (random.nextGaussian() * 10) + center;
    }
}
