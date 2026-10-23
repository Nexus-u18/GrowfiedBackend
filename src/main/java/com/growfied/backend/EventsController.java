
package com.growfied.backend;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:4173",
    "https://growfied-new-one.vercel.app"
})
public class EventsController {

    private final EventRepository eventRepository;

    public EventsController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public Map<String, Object> getEvents() {
        List<Map<String, Object>> result =
                eventRepository.findAllByOrderByDateAscStartAsc()
                        .stream()
                        .map(this::toMap)
                        .toList();

        return Map.of("success", true, "data", result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(
            @RequestBody Map<String, Object> body) {

        if (!isValid(body)) {
            return badRequest();
        }

        Event event = new Event();
        updateFields(event, body);

        Event saved = eventRepository.save(event);

        return ResponseEntity.ok(
                Map.of("success", true, "data", toMap(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        var optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isEmpty()) {
            return ResponseEntity.status(404).body(
                    Map.of("success", false,
                           "message", "Event not found."));
        }

        if (!isValid(body)) {
            return badRequest();
        }

        Event event = optionalEvent.get();
        updateFields(event, body);

        Event saved = eventRepository.save(event);

        return ResponseEntity.ok(
                Map.of("success", true, "data", toMap(saved)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(
            @PathVariable Long id) {

        if (!eventRepository.existsById(id)) {
            return ResponseEntity.status(404).body(
                    Map.of("success", false,
                           "message", "Event not found."));
        }

        eventRepository.deleteById(id);

        return ResponseEntity.ok(
                Map.of("success", true,
                       "message", "Event deleted."));
    }

    private void updateFields(Event event, Map<String, Object> body) {
        event.setTitle(body.get("title").toString().trim());
        event.setDescription(
                body.getOrDefault("description", "").toString());
        event.setDate(LocalDate.parse(body.get("date").toString()));
        event.setStart(LocalTime.parse(body.get("start").toString()));
        event.setEnd(LocalTime.parse(body.get("end").toString()));
        event.setCategory(
                body.getOrDefault("category", "Study").toString());
        event.setAlert(Boolean.TRUE.equals(body.get("alert")));
    }

    private Map<String, Object> toMap(Event event) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", event.getId());
        result.put("title", event.getTitle());
        result.put("description", event.getDescription());
        result.put("date", event.getDate().toString());
        result.put("start", event.getStart().toString());
        result.put("end", event.getEnd().toString());
        result.put("category", event.getCategory());
        result.put("alert", event.isAlert());

        return result;
    }

    private boolean isValid(Map<String, Object> body) {
        try {
            if (body == null
                    || body.get("title") == null
                    || body.get("date") == null
                    || body.get("start") == null
                    || body.get("end") == null) {
                return false;
            }

            String title = body.get("title").toString().trim();

            return !title.isEmpty()
                    && title.length() <= 100
                    && LocalDate.parse(body.get("date").toString()) != null
                    && LocalTime.parse(body.get("start").toString())
                            .isBefore(LocalTime.parse(
                                    body.get("end").toString()));
        } catch (Exception e) {
            return false;
        }
    }

    private ResponseEntity<Map<String, Object>> badRequest() {
        return ResponseEntity.badRequest().body(
                Map.of("success", false,
                       "message",
                       "Please provide a valid title, date and time."));
    }
}
