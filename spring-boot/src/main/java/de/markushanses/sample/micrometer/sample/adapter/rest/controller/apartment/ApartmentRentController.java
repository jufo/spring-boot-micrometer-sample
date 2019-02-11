package de.markushanses.sample.micrometer.sample.adapter.rest.controller.apartment;

import de.markushanses.sample.micrometer.sample.adapter.rest.exception.ApartmentNotFoundException;
import de.markushanses.sample.micrometer.sample.domain.apartment.Apartment;
import de.markushanses.sample.micrometer.sample.domain.apartment.ApartmentService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(path = "api/apartment-rent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ApartmentRentController {

    private final Counter rentSuccessCounter;
    private final Counter startRentCounter;
    private final Counter cancelCounter;
    private final AtomicInteger rentProgress;
    private final Timer rentTimer;
    private final Random random;

    private final ApartmentService apartmentService;
    private final ApartmentResourceMapper apartmentResourceMapper;

    @Autowired
    public ApartmentRentController(MeterRegistry registry, ApartmentService apartmentService, ApartmentResourceMapper apartmentResourceMapper) {
        this.startRentCounter = registry.counter("counter.apartment.rent.start", "object_type", "apartment", "rent_process_state", "started");
        this.rentSuccessCounter = registry.counter("counter.apartment.rent.success", "object_type", "apartment", "rent_process_state", "success");
        this.cancelCounter = registry.counter("counter.apartment.rent.cancel", "object_type", "apartment", "rent_process_state", "aborted");
        this.rentProgress = registry.gauge("gauge.apartment.rent.progress", Tags.of("object_type", "apartment", "rent_process_state", "ongoing"), new AtomicInteger(0));
        this.rentTimer = Timer.builder("timer.apartment.rent").publishPercentileHistogram().register(registry);
        this.apartmentService = apartmentService;
        this.apartmentResourceMapper = apartmentResourceMapper;
        this.random = new Random();
    }

    @ApiOperation(value = "Reserves an apartment")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Apartment successfully reserved.")
    })
    @PostMapping(path = "/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApartmentResource> reserveApartment(@RequestBody ApartmentReserveResource reserveResource) {
        Apartment apartment = apartmentService.reserveApartment(reserveResource.getApartmentId());
        ApartmentResource outputResource = apartmentResourceMapper.mapApartmentToResource(apartment);

        startRentCounter.increment();
        rentProgress.incrementAndGet();

        return new ResponseEntity<>(outputResource, HttpStatus.CREATED);
    }

    @ApiOperation(value = "Confirms an apartment reservation")
    @PostMapping(path = "/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApartmentResource> rentApartment(@RequestBody ApartmentResource resource) {
        if (resource.getApartmentId().equals(UUID.fromString("6226560e-2e9a-4dad-8854-f996ed47e250"))) {
            throw new ApartmentNotFoundException("No apartment found");
        }
        rentProgress.decrementAndGet();
        rentSuccessCounter.increment();
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }


    @ApiOperation(value = "Cancels an apartment reservation or an rent")
    @PostMapping(path = "/cancel")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApartmentResource> cancelRentApartment(@RequestBody ApartmentResource resource) {
        if (resource.getApartmentId().equals(UUID.fromString("6226560e-2e9a-4dad-8854-f996ed47e250"))) {
            throw new ApartmentNotFoundException("No apartment found");
        }
        cancelCounter.increment();
        rentProgress.decrementAndGet();
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /*@GetMapping("/delay/{delayInSeconds}")
    public ResponseEntity<String> cancelRentApartmentWithDelay(@PathVariable Long delayInSeconds) {
        rentTimer.record(simulatedLatency(delayInSeconds), TimeUnit.SECONDS);
        return new ResponseEntity<>("Apartment delay ID: " + Clock.systemDefaultZone().millis(), HttpStatus.OK);
    }

    private long simulatedLatency(long center) {
        return (long) (random.nextGaussian() * 10) + center;
    }*/
}
