
package com.growfied.backend;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:4173",
    "https://growfied-new-one.vercel.app"
})
public class ProgressController {

    @GetMapping
    public Map<String, Object> getProgress() {

        Map<String, Object> progress = Map.of(
            "mockTests", 0,
            "questionsSolved", 0,
            "currentProjects", 0
        );

        return Map.of(
            "success", true,
            "data", progress
        );
    }
}
