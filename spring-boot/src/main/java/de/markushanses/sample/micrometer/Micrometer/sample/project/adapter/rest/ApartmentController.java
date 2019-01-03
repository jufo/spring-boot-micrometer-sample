package de.markushanses.sample.micrometer.Micrometer.sample.project.adapter.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(path = "api/apartment")
public class ApartmentController {

    @GetMapping
    public ResponseEntity<List<ApartmentResource>> retrieveAll() {
        return new ResponseEntity<>(
                createApartmentList(),
                HttpStatus.OK);
    }

    private List<ApartmentResource> createApartmentList() {
        ApartmentResource apartmentResourceOne = new ApartmentResource();
        apartmentResourceOne.setApartmentId(1L);
        apartmentResourceOne.setCity("Hamburg");
        apartmentResourceOne.setStatus(ApartmentResource.Status.reserved);
        ApartmentResource apartmentResourceTwo = new ApartmentResource();
        apartmentResourceTwo.setApartmentId(2L);
        apartmentResourceTwo.setCity("Berlin");
        apartmentResourceTwo.setStatus(ApartmentResource.Status.free);
        return List.of(apartmentResourceOne, apartmentResourceTwo);
    }

}
