package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.WallEstimationRequest;
import com.ssnc.schemaService.dto.WallEstimationResponse;
import com.ssnc.schemaService.util.ConversionUtil;
import org.springframework.stereotype.Service;

@Service
public class EstimationService {

    private static final double BRICK_WASTAGE_FACTOR = 0.05;
    private static final double WET_MORTAR_FACTOR = 0.3;
    private static final double DRY_MORTAR_FACTOR = 1.33;
    private static final double CEMENT_BAG_VOLUME_M3 = 0.035;
    private static final double SAND_DENSITY_KG_PER_M3 = 1600;
    private static final double CUBIC_METER_TO_CUBIC_FEET = 35.3147;

    public WallEstimationResponse calculateWallEstimation(WallEstimationRequest request) {
        double wallVolume = wallVolume(request);
        double brickQty = brickQuantity(wallVolume, request);

        double dryMortarVolume = WET_MORTAR_FACTOR * DRY_MORTAR_FACTOR * wallVolume;


        double totalRatio = request.getCementRatio()+ request.getSandRatio();
        double cementVolume = ratioShare(dryMortarVolume, request.getCementRatio(), totalRatio);

        double sandVolume = ratioShare(dryMortarVolume, request.getSandRatio(), totalRatio);

        double cementBagsQty = cementVolume / CEMENT_BAG_VOLUME_M3;

        double sandQty = sandVolume * SAND_DENSITY_KG_PER_M3;


        double cementCost = cementBagsQty * request.getCementBagPrice();
        double brickCost = brickQty * request.getBrickPricePerBrick();
        double sandCost = sandVolume * request.getSandPricePerCubicMeter();
        double labourCost = request.getLaborCostPerSquareFoot() * wallVolume * CUBIC_METER_TO_CUBIC_FEET;
        double totalCost = cementCost + brickCost + sandCost + labourCost;

        return buildModel(wallVolume,
                brickQty, cementBagsQty, sandQty,
                brickCost, cementCost, sandCost,
                labourCost, totalCost);
    }

    private static double wallVolume(WallEstimationRequest req) {
        return readVolume(req.getWallLength(), req.getWallLengthUnit(),
                          req.getWallHeight(), req.getWallHeightUnit(),
                          req.getWallThickness(), req.getWallThicknessUnit());
    }

    private static double brickVolume(WallEstimationRequest req) {
        return readVolume(req.getBrickLength(), req.getBrickLengthUnit(),
                          req.getBrickHeight(), req.getBrickHeightUnit(),
                          req.getBrickWidth(), req.getBrickWidthUnit());
    }

    private static double brickQuantity(double wallVolume, WallEstimationRequest req) {
        double brickVolume = brickVolume(req);
        if (brickVolume == 0) return 0;
        return (wallVolume / brickVolume) * (1 + BRICK_WASTAGE_FACTOR);
    }

    private static double ratioShare(double total, double part, double sum) {
        return sum == 0 ? 0 : (part / sum) * total;
    }

    private static double readVolume(int lenField, String lenUnit,
                                     int heightField, String heightUnit,
                                     int widthField, String widthUnit) {
        double l = ConversionUtil.convertToMeter(lenField, lenUnit);
        double h = ConversionUtil.convertToMeter(heightField, heightUnit);
        double w = ConversionUtil.convertToMeter(widthField, widthUnit);
        return l * h * w;
    }

    private static WallEstimationResponse buildModel(double wallVolume,
                                        double brickQty, double cementQty, double sandQty,
                                        double brickCost, double cementCost, double sandCost,
                                        double labourCost, double totalCost) {
        WallEstimationResponse m = new WallEstimationResponse();
        m.setWallVolume(round2(wallVolume));
        m.setBrickQty(round2(brickQty));
        m.setCementQty(round2(cementQty));
        m.setSandQty(round2(sandQty));
        m.setBrickCost(round2(brickCost));
        m.setCementCost(round2(cementCost));
        m.setSandCost(round2(sandCost));
        m.setLabourCost(round2(labourCost));
        m.setTotalCost(round2(totalCost));
        return m;
    }

    private static String round2(double value) {
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }
}