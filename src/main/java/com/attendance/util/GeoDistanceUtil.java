package com.attendance.util;

public final class GeoDistanceUtil {
    private GeoDistanceUtil() { }
    public static double calculateMeters(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6_371_000, dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
