package com.attendance.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendance.dto.request.MarkAttendanceRequest;
import com.attendance.dto.response.AttendanceResultResponse;
import com.attendance.mapper.AttendanceMapper;
import com.attendance.service.AttendanceService;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceApiController {
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    public AttendanceApiController(AttendanceService attendanceService, AttendanceMapper attendanceMapper) {
        this.attendanceService = attendanceService;
        this.attendanceMapper = attendanceMapper;
    }

    @PostMapping("/punch")
    public AttendanceResultResponse punch(@RequestBody MarkAttendanceRequest request) {
        return attendanceMapper.toResponse(attendanceService.markAttendance(request));
    }
}
