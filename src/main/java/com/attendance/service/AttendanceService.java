package com.attendance.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.attendance.dto.request.MarkAttendanceRequest;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.AttendanceRecordEntity;
import com.attendance.entity.AttendanceType;
import com.attendance.entity.DemoLocation;
import com.attendance.entity.DemoUser;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.AttendanceRecordJpaRepository;
import com.attendance.repository.DemoDataRepository;
import com.attendance.util.GeoDistanceUtil;

@Service
public class AttendanceService {
    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final DemoDataRepository demoDataRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceRecordJpaRepository attendanceRecordJpaRepository;
    private final FaceVerificationService faceVerificationService;
    private final LivenessChallengeService livenessChallengeService;

    public AttendanceService(DemoDataRepository demoDataRepository, AttendanceRepository attendanceRepository,
                             FaceVerificationService faceVerificationService,
                             AttendanceRecordJpaRepository attendanceRecordJpaRepository,
                             LivenessChallengeService livenessChallengeService) {
        this.demoDataRepository = demoDataRepository;
        this.attendanceRepository = attendanceRepository;
        this.faceVerificationService = faceVerificationService;
        this.attendanceRecordJpaRepository = attendanceRecordJpaRepository;
        this.livenessChallengeService = livenessChallengeService;
    }
    public List<DemoUser> getUsers() { return demoDataRepository.findUsers(); }
    public List<DemoLocation> getLocations() { return demoDataRepository.findLocations(); }
    public List<AttendanceRecord> getAttendanceRecords() { return attendanceRepository.findAll(); }
    @Transactional
    public AttendanceDecision markAttendance(MarkAttendanceRequest request) {
        if (request == null || !Double.isFinite(request.getLatitude()) || !Double.isFinite(request.getLongitude())
                || request.getLatitude() < -90 || request.getLatitude() > 90
                || request.getLongitude() < -180 || request.getLongitude() > 180
                || (request.getLatitude() == 0.0 && request.getLongitude() == 0.0))
            return AttendanceDecision.rejected("Valid GPS coordinates are required.");
        log.info("Attendance punch requested: userCode={}, locationCode={}, type={}, latitude={}, longitude={}",
                request.getUserCode(), request.getLocationCode(), request.getAttendanceType(),
                request.getLatitude(), request.getLongitude());
        DemoUser user = demoDataRepository.findUser(request.getUserCode()).orElse(null);
        DemoLocation location = demoDataRepository.findLocation(request.getLocationCode()).orElse(null);
        if (user == null || location == null) {
            log.warn("Attendance rejected: invalid user or location, userCode={}, locationCode={}", request.getUserCode(), request.getLocationCode());
            return AttendanceDecision.rejected("Invalid demo user or location.");
        }
        AttendanceType type;
        try { type = AttendanceType.valueOf(request.getAttendanceType().toUpperCase()); }
        catch (Exception ex) {
            log.warn("Attendance rejected: invalid punch type, userCode={}, type={}", request.getUserCode(), request.getAttendanceType());
            return AttendanceDecision.rejected("Select a valid attendance type: IN or OUT.");
        }
        // DB idempotency key source of truth hai. In-memory cache process
        // restart/delete ke baad stale ho sakta hai, isliye usse duplicate
        // attendance decide nahi karte.
        String today = LocalDate.now().toString();
        if (attendanceRecordJpaRepository.existsByIdempotencyKey(user.code() + "-" + today + "-" + type))
            return reject("Today's " + type + " punch is already recorded for this user.", user.code(), type);
        if (type == AttendanceType.OUT
                && !attendanceRecordJpaRepository.existsByIdempotencyKey(user.code() + "-" + today + "-IN"))
            return reject("OUT punch rejected: first record an IN punch.", user.code(), type);
        if (!user.assignedLocationCode().equals(location.code())) return reject("Attendance rejected: user is not assigned to this location.", user.code(), type);
        log.info("Attendance assignment validated: userCode={}, assignedLocation={}, requestedLocation={}", user.code(), user.assignedLocationCode(), location.code());

        // GPS reject hone par expensive liveness proof consumption aur
        // ArcFace verification chalane ki zaroorat nahi hai. Isse invalid
        // location attempts fast reject hote hain aur liveness retry bachta hai.
        double distance = GeoDistanceUtil.calculateMeters(request.getLatitude(), request.getLongitude(), location.latitude(), location.longitude());
        log.info("Attendance GPS validation: userCode={}, locationCode={}, distanceMeters={}, allowedRadiusMeters={}", user.code(), location.code(), distance, location.radiusMeters());
        if (distance > location.radiusMeters()) return reject(String.format("Attendance rejected: %.1fm is outside the %.0fm radius.", distance, location.radiusMeters()), user.code(), type);

        // Face verification check BEFORE consuming liveness token to preserve token on face verification failure
        boolean faceVerified = request.getFaceImageBase64() != null && !request.getFaceImageBase64().isBlank()
                && faceVerificationService.verifyImage(user, request.getFaceImageBase64(), request.getSourceImageBase64());
        if (!faceVerified) return reject("Attendance rejected: face verification failed or user is not enrolled.", user.code(), type);
        // Only consume liveness token after successful face verification
        if (!livenessChallengeService.consume(request.getLivenessSessionId(), user.code()))
            return reject("Attendance rejected: valid one-time liveness proof is required.", user.code(), type);
        AttendanceRecord record = new AttendanceRecord(user.code(), user.name(), location.code(), type,
                request.getLatitude(), request.getLongitude(), location.latitude(), location.longitude(),
                distance, location.radiusMeters(), LocalDateTime.now());
        // DB is the source of truth. Flush first so a NOT NULL/constraint/
        // connectivity problem cannot leave a record visible only in memory.
        AttendanceRecordEntity savedEntity = attendanceRecordJpaRepository.saveAndFlush(new AttendanceRecordEntity(record));
        attendanceRepository.save(record);
        log.info("Attendance database record saved: databaseId={}, userCode={}, type={}, locationCode={}, idempotencyKey={}",
                savedEntity.getId(), user.code(), type, location.code(),
                user.code() + "-" + record.recordedAt().toLocalDate() + "-" + type);
        log.info("Attendance punch accepted: userCode={}, type={}, locationCode={}, distanceMeters={}, recordedAt={}", user.code(), type, location.code(), distance, record.recordedAt());
        return AttendanceDecision.accepted(record);
    }

    private AttendanceDecision reject(String message, String userCode, AttendanceType type) {
        log.warn("Attendance punch rejected: userCode={}, type={}, reason={}", userCode, type, message);
        return AttendanceDecision.rejected(message);
    }
}
