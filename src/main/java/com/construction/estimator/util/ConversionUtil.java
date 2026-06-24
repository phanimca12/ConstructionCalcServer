package com.construction.estimator.util;

public class ConversionUtil {

    public static double convertToMeter(int length,String unit)
    {
        switch (unit.toLowerCase()) {
            case "m":
                return length;
            case "cm":
                return (double) length / 100;
            case "mm":
                return (double)length / 1000;
            case "ft":
                return (double) (length / 3.281);
            case "inch":
                return (double) (length / 39.37);
            case "yrd":
                return (double) (length / 1.094);
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }

    }

    public static double volumeOfWall(double length,double height,double thickness)
    {
        return length * height * thickness;
    }
}
