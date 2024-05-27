package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.data.ScheduleDAO;
import com.udacity.jdnd.course3.critter.schedule.ScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Transactional
@Service
public class ScheduleService {
    @Autowired
    private ScheduleDAO scheduleDAO;

    public ScheduleDTO addSchedule(ScheduleDTO scheduleDTO) {
        if (Objects.isNull(scheduleDTO))
            throw new RuntimeException();

        Long newId = scheduleDAO.createNewSchedule(scheduleDTO);
        scheduleDTO.setId(newId);
        return scheduleDAO.getScheduleById(newId);
    }

    public List<ScheduleDTO> getAllSchedules() {
        return scheduleDAO.getAllSchedules();
    }

    public List<ScheduleDTO> getScheduleByPetId(Long petId) {
        return scheduleDAO.getScheduleByPetId(petId);
    }

    public List<ScheduleDTO> getScheduleByEmployeeId(Long employeeId) {
        return scheduleDAO.getScheduleByEmployeeId(employeeId);
    }

    public List<ScheduleDTO> getScheduleByCustomerId(Long customerId) {
        return scheduleDAO.getScheduleByCustomerId(customerId);
    }
}
