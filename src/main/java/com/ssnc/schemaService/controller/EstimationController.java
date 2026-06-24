package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.WallEstimationRequest;
import com.ssnc.schemaService.dto.WallEstimationResponse;
import com.ssnc.schemaService.service.EstimationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estimate")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstimationController {

    @Autowired
    EstimationService wallEstimationService;

    @PostMapping(value="/wall", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WallEstimationResponse estimate(
            @RequestBody WallEstimationRequest request) {

        return wallEstimationService.calculateWallEstimation(request);
    }

}