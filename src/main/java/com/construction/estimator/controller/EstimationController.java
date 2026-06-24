package com.construction.estimator.controller;


import com.construction.estimator.dto.WallEstimationRequest;
import com.construction.estimator.dto.WallEstimationResponse;
import com.construction.estimator.service.EstimationService;
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