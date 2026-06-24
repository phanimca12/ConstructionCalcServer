package com.ssnc.schemaService.dto;

public class WallEstimationRequest {

    int wallLength;
    String wallLengthUnit;
    int cementRatio;
    int sandRatio;
    int wallHeight;
    String wallHeightUnit;
    int wallThickness;
    String wallThicknessUnit;
    int brickLength;
    String brickLengthUnit;
    int brickHeight;
    String brickHeightUnit;
    int brickWidth;
    String brickWidthUnit;
    double cementBagPrice;
    double sandPricePerCubicMeter;
    double brickPricePerBrick;
    double laborCostPerSquareFoot;

    public int getCementRatio() {
        return cementRatio;
    }

    public void setCementRatio(int cementRatio) {
        this.cementRatio = cementRatio;
    }

    public int getSandRatio() {
        return sandRatio;
    }

    public void setSandRatio(int sandRatio) {
        this.sandRatio = sandRatio;
    }


    public int getWallLength() {
        return wallLength;
    }

    public void setWallLength(int wallLength) {
        this.wallLength = wallLength;
    }

    public String getWallLengthUnit() {
        return wallLengthUnit;
    }

    public void setWallLengthUnit(String wallLengthUnit) {
        this.wallLengthUnit = wallLengthUnit;
    }

    public int getWallHeight() {
        return wallHeight;
    }

    public void setWallHeight(int wallHeight) {
        this.wallHeight = wallHeight;
    }

    public String getWallHeightUnit() {
        return wallHeightUnit;
    }

    public void setWallHeightUnit(String wallHeightUnit) {
        this.wallHeightUnit = wallHeightUnit;
    }

    public int getWallThickness() {
        return wallThickness;
    }

    public void setWallThickness(int wallThickness) {
        this.wallThickness = wallThickness;
    }

    public String getWallThicknessUnit() {
        return wallThicknessUnit;
    }

    public void setWallThicknessUnit(String wallThicknessUnit) {
        this.wallThicknessUnit = wallThicknessUnit;
    }

    public int getBrickLength() {
        return brickLength;
    }

    public void setBrickLength(int brickLength) {
        this.brickLength = brickLength;
    }

    public String getBrickLengthUnit() {
        return brickLengthUnit;
    }

    public void setBrickLengthUnit(String brickLengthUnit) {
        this.brickLengthUnit = brickLengthUnit;
    }

    public int getBrickHeight() {
        return brickHeight;
    }

    public void setBrickHeight(int brickHeight) {
        this.brickHeight = brickHeight;
    }

    public String getBrickHeightUnit() {
        return brickHeightUnit;
    }

    public void setBrickHeightUnit(String brickHeightUnit) {
        this.brickHeightUnit = brickHeightUnit;
    }

    public int getBrickWidth() {
        return brickWidth;
    }

    public void setBrickWidth(int brickWidth) {
        this.brickWidth = brickWidth;
    }

    public String getBrickWidthUnit() {
        return brickWidthUnit;
    }

    public void setBrickWidthUnit(String brickWidthUnit) {
        this.brickWidthUnit = brickWidthUnit;
    }

    public double getCementBagPrice() {
        return cementBagPrice;
    }

    public void setCementBagPrice(double cementBagPrice) {
        this.cementBagPrice = cementBagPrice;
    }

    public double getSandPricePerCubicMeter() {
        return sandPricePerCubicMeter;
    }

    public void setSandPricePerCubicMeter(double sandPricePerCubicMeter) {
        this.sandPricePerCubicMeter = sandPricePerCubicMeter;
    }

    public double getBrickPricePerBrick() {
        return brickPricePerBrick;
    }

    public void setBrickPricePerBrick(double brickPricePerBrick) {
        this.brickPricePerBrick = brickPricePerBrick;
    }

    public double getLaborCostPerSquareFoot() {
        return laborCostPerSquareFoot;
    }

    public void setLaborCostPerSquareFoot(double laborCostPerSquareFoot) {
        this.laborCostPerSquareFoot = laborCostPerSquareFoot;
    }


}
