package com.smartdine.service;

import com.smartdine.dto.DashboardStatsDto;
import com.smartdine.model.Establishment;
import com.smartdine.model.Person;
import com.smartdine.model.UserStatus;

import java.util.List;

public interface AdminService {
    DashboardStatsDto getPlatformAnalytics(String timeRange);
    List<Person> getAllUsers(String search);
    Person updateUserStatus(Long userId, UserStatus status);
    List<Establishment> getAllEstablishments();
    Establishment updateEstablishmentStatus(Long establishmentId, UserStatus status);
}
