package com.ssnc.schemaService.dto;

public class WallEstimationResponse {

    String wallVolume;
    String brickQty;
    String cementQty;
    String sandQty;
    String brickCost;
    String cementCost;
    String sandCost;
    String labourCost;
    String totalCost;

    public String getWallVolume() {
        return wallVolume;
    }

    public void setWallVolume(String wallVolume) {
        this.wallVolume = wallVolume;
    }

    public String getBrickQty() {
        return brickQty;
    }

    public void setBrickQty(String brickQty) {
        this.brickQty = brickQty;
    }

    public String getCementQty() {
        return cementQty;
    }

    public void setCementQty(String cementQty) {
        this.cementQty = cementQty;
    }

    public String getSandQty() {
        return sandQty;
    }

    public void setSandQty(String sandQty) {
        this.sandQty = sandQty;
    }

    public String getBrickCost() {
        return brickCost;
    }

    public void setBrickCost(String brickCost) {
        this.brickCost = brickCost;
    }

    public String getCementCost() {
        return cementCost;
    }

    public void setCementCost(String cementCost) {
        this.cementCost = cementCost;
    }

    public String getSandCost() {
        return sandCost;
    }

    public void setSandCost(String sandCost) {
        this.sandCost = sandCost;
    }

    public String getLabourCost() {
        return labourCost;
    }

    public void setLabourCost(String labourCost) {
        this.labourCost = labourCost;
    }

    public String getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }
}
