
package com.growfied.backend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:4173",
    "https://growfied-new-one.vercel.app"
})
public class EventsController {

    private final Map<Long, Map<String, Object>> events =
            new ConcurrentHashMap<>();

    private final AtomicLong nextId = new AtomicLong(3);

    public EventsController() {
        addSampleEvent(1, "GrowFied Launch",
                "Explore opportunities with GrowFied",
                "2026-10-25", "10:00", "11:00", "Milestone", false);

        addSampleEvent(2, "Student Skill Session",
                "Build skills and connect with students",
                "2026-10-26", "14:00", "15:00", "Study", true);
    }

    private void addSampleEvent(
            long id, String title, String description,
            String date, String start, String end,
            String category, boolean alert) {

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("id", id);
        event.put("title", title);
        event.put("description", description);
        event.put("date", date);
        event.put("start", start);
        event.put("end", end);
        event.put("category", category);
        event.put("alert", alert);

        events.put(id, event);
    }

    @GetMapping
    public Map<String, Object> getEvents() {
        List<Map<String, Object>> result =
                new ArrayList<>(events.values());

        result.sort((a, b) -> {
            int dateComparison =
                    a.get("date").toString()
                            .compareTo(b.get("date").toString());

            if (dateComparison != 0) {
                return dateComparison;
            }

            return a.get("start").toString()
                    .compareTo(b.get("start").toString());
        });

        return Map.of(
                "success", true,
                "data", result
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(
            @RequestBody Map<String, Object> body) {

        if (!isValid(body)) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false,
                           "message", "Please provide a valid title, date and time.")
            );
        }

        long id = nextId.getAndIncrement();

        Map<String, Object> event = createEventData(id, body);
        events.put(id, event);

        return ResponseEntity.ok(
                Map.of("success", true, "data", event)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        if (!events.containsKey(id)) {
            return ResponseEntity.status(404).body(
                    Map.of("success", false,
                           "message", "Event not found.")
            );
        }

        if (!isValid(body)) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false,
                           "message", "Please provide a valid title, date and time.")
            );
        }

        Map<String, Object> event = createEventData(id, body);
        events.put(id, event);

        return ResponseEntity.ok(
                Map.of("success", true, "data", event)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEvent(
            @PathVariable Long id) {

        if (events.remove(id) == null) {
            return ResponseEntity.status(404).body(
                    Map.of("success", false,
                           "message", "Event not found.")
            );
        }

        return ResponseEntity.ok(
                Map.of("success", true, "message", "Event deleted.")
        );
    }

    private Map<String, Object> createEventData(
            long id, Map<String, Object> body) {

        Map<String, Object> event = new LinkedHashMap<>();

        event.put("id", id);
        event.put("title", body.get("title").toString().trim());
        event.put("description",
                body.getOrDefault("description", "").toString());
        event.put("date", body.get("date").toString());
        event.put("start", body.get("start").toString());
        event.put("end", body.get("end").toString());
        event.put("category",
                body.getOrDefault("category", "Study").toString());
        event.put("alert", Boolean.TRUE.equals(body.get("alert")));

        return event;
    }

    private boolean isValid(Map<String, Object> body) {
        try {
            String title = body.get("title").toString().trim();
            String date = body.get("date").toString();
            String start = body.get("start").toString();
            String end = body.get("end").toString();

            return !title.isEmpty()
                    && title.length() <= 100
                    && java.time.LocalDate.parse(date) != null
                    && java.time.LocalTime.parse(start)
                            .isBefore(java.time.LocalTime.parse(end));
        } catch (Exception e) {
            return false;
        }
    }
}
