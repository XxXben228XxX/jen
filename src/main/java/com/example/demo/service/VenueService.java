package com.example.demo.service;

import com.example.demo.model.Venue;
import com.example.demo.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    @Transactional(readOnly = true)
    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    @Transactional
    public Venue updateVenue(Long id, Venue updatedVenue) {
        return venueRepository.findById(id)
                .map(venue -> {
                    // Оновлюємо поля існуючої сутності 'venue' даними з 'updatedVenue'
                    // Замість updatedVenue.setId(id);
                    // Вам потрібно оновити поля, які можуть змінюватися.
                    // Наприклад, якщо у Venue є поле 'name' і 'address':
                    venue.setName(updatedVenue.getName()); // Припустимо, у Venue є getName() і setName()
                    venue.setAddress(updatedVenue.getAddress()); // Припустимо, у Venue є getAddress() і setAddress()
                    // Додайте тут всі інші поля, які ви хочете оновити
                    // venue.setSomeOtherField(updatedVenue.getSomeOtherField());

                    return venueRepository.save(venue); // Зберігаємо оновлену існуючу сутність
                })
                .orElse(null); // Якщо Venue з таким ID не знайдено, повертаємо null
    }

    @Transactional
    public boolean deleteVenue(Long id) {
        if (venueRepository.existsById(id)) {
            venueRepository.deleteById(id);
            return true;
        }
        return false;
    }
}