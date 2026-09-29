package com.attendance.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.attendance.entity.DemoLocation;
import com.attendance.entity.DemoUser;

@Repository
public class DemoDataRepository {
    private final List<DemoUser> users = List.of(
            new DemoUser("EMP001", "Amit Sharma", "CAMP004"),
            new DemoUser("EMP002", "Priya Verma", "CAMP004"),
            new DemoUser("EMP003", "Ravi Kumar", "CAMP004"),
            new DemoUser("EMP004", "Sajjan", "CAMP004"),
            new DemoUser("EMP005", "Deepak", "CAMP004"),
            new DemoUser("EMP006", "Ayusman", "CAMP004"),
            new DemoUser("EMP007", "Ritesh", "CAMP004"),
            new DemoUser("EMP008", "Sipu", "CAMP004"),
            new DemoUser("EMP009", "Sarang", "CAMP004"),
            new DemoUser("EMP010", "Khana", "CAMP004"),
            new DemoUser("EMP011", "Manash", "CAMP004"),
            new DemoUser("EMP012", "Shivdeep", "CAMP004"));
    private final List<DemoLocation> locations = List.of(
            new DemoLocation("CAMP004", "Current Mobile Camp", 21.2146664, 81.6545622, 50));
    public List<DemoUser> findUsers() { return users; }
    public List<DemoLocation> findLocations() { return locations; }
    public Optional<DemoUser> findUser(String code) { return users.stream().filter(u -> u.code().equals(code)).findFirst(); }
    public Optional<DemoLocation> findLocation(String code) { return locations.stream().filter(l -> l.code().equals(code)).findFirst(); }
}
