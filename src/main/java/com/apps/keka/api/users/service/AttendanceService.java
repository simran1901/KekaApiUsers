package com.apps.keka.api.users.service;

import com.apps.keka.api.users.shared.AttendanceDto;
import com.apps.keka.api.users.ui.model.AttendanceResponseModel;
import com.apps.keka.api.users.ui.model.GetAttendanceRequestModel;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public interface AttendanceService {
    AttendanceDto markAttendance(AttendanceDto details);

    Date getCurrentDate();

    Time getCurrentTime();

    void checkIn(String userId);

    void checkOut(String userId);

    public List<AttendanceResponseModel> getAttendance(String userId, GetAttendanceRequestModel details);
}
