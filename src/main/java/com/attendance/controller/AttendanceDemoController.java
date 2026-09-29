package com.attendance.controller;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.attendance.dto.request.MarkAttendanceRequest;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.service.AttendanceService;

@Controller
public class AttendanceDemoController {
    private static final Logger log = LoggerFactory.getLogger(AttendanceDemoController.class);
    private static final String UI_VERSION = "attendance-ui-2026-09-29-1505";
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    public AttendanceDemoController(AttendanceService attendanceService, AttendanceMapper attendanceMapper) {
        this.attendanceService = attendanceService;
        this.attendanceMapper = attendanceMapper;
    }

    @GetMapping("/demo/attendance")
    public String page(Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Attendance-UI-Version", UI_VERSION);
        response.setDateHeader("Expires", 0);
        log.info("Attendance demo page rendered: uiVersion={}", UI_VERSION);
        model.addAttribute("users", attendanceService.getUsers());
        model.addAttribute("locations", attendanceService.getLocations());
        model.addAttribute("records", attendanceMapper.toResponseList(attendanceService.getAttendanceRecords()));
        return "attendance-demo";
    }

    // Keep the legacy path temporarily compatible with cached JSP pages while
    // the current page uses /demo/attendance/submit.
    @PostMapping({"/demo/attendance", "/demo/attendance/submit"})
    public String mark(
            @RequestParam(name = "userCode", required = false) String userCode,
            @RequestParam(name = "locationCode", required = false) String locationCode,
            @RequestParam(name = "latitude", required = false) String latitudeValue,
            @RequestParam(name = "longitude", required = false) String longitudeValue,
            @RequestParam(name = "attendanceType", required = false) String attendanceType,
            @RequestParam(name = "faceImageBase64", required = false) String faceImageBase64,
            @RequestParam(name = "sourceImageBase64", required = false) String sourceImageBase64,
            @RequestParam(name = "livenessSessionId", required = false) String livenessSessionId,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Model model) {
        double latitude = parseCoordinate(latitudeValue);
        double longitude = parseCoordinate(longitudeValue);
        log.info("Demo attendance HTTP request: method={}, uri={}, contentType={}, contentLength={}, queryString={}, parameterNames={}",
                httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getContentType(),
                httpRequest.getContentLengthLong(), httpRequest.getQueryString(),
                Arrays.toString(httpRequest.getParameterMap().keySet().toArray()));
        log.info("Demo attendance raw fields: userCode={}, locationCode={}, attendanceType={}, rawLatitude={}, rawLongitude={}, latitude={}, longitude={}, facePresent={}, sourcePresent={}, livenessPresent={}",
                userCode, locationCode, attendanceType, latitudeValue, longitudeValue, latitude, longitude,
                faceImageBase64 != null && !faceImageBase64.isBlank(),
                sourceImageBase64 != null && !sourceImageBase64.isBlank(),
                livenessSessionId != null && !livenessSessionId.isBlank());
        log.info("Demo attendance GPS received: latitude={}, longitude={}, validRange={}",
                latitude, longitude,
                Double.isFinite(latitude) && Double.isFinite(longitude)
                        && latitude >= -90 && latitude <= 90
                        && longitude >= -180 && longitude <= 180
                        && !(latitude == 0.0 && longitude == 0.0));
        if (userCode == null && locationCode == null && attendanceType == null
                && latitudeValue == null && longitudeValue == null
                && faceImageBase64 == null && sourceImageBase64 == null
                && livenessSessionId == null) {
            log.warn("Empty legacy attendance POST detected; redirecting to clean GET page.");
            return "redirect:/demo/attendance";
        }
        // Single-CAMP004 demo fallback for older cached pages that omit the
        // select values. GPS and face validation still remain mandatory.
        if (userCode == null || userCode.isBlank()) {
            log.warn("Demo attendance userCode missing; using demo default EMP004.");
            userCode = "EMP004";
        }
        if (locationCode == null || locationCode.isBlank()) {
            log.warn("Demo attendance locationCode missing; using demo default CAMP004.");
            locationCode = "CAMP004";
        }
        if (attendanceType == null || attendanceType.isBlank()) attendanceType = "IN";
        MarkAttendanceRequest request = new MarkAttendanceRequest();
        request.setUserCode(userCode);
        request.setLocationCode(locationCode);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setAttendanceType(attendanceType);
        request.setFaceImageBase64(faceImageBase64);
        request.setSourceImageBase64(sourceImageBase64);
        request.setLivenessSessionId(livenessSessionId);
        model.addAttribute("result", attendanceMapper.toResponse(
                attendanceService.markAttendance(request)));
        return page(model, httpResponse);
    }

    private double parseCoordinate(String value) {
        if (value == null || value.isBlank()) return Double.NaN;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return Double.NaN;
        }
    }
}
