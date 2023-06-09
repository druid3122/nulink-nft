package com.project.nulinknft.controller;

import com.project.nulinknft.service.BlindBoxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlindBoxController {

    private final BlindBoxService blindBoxService;

    public BlindBoxController(BlindBoxService blindBoxService) {
        this.blindBoxService = blindBoxService;
    }

    /*@GetMapping("referPage")
    public Page<>*/
}
