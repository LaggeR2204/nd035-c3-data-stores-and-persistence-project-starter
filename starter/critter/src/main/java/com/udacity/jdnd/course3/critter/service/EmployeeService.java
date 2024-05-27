package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.data.EmployeeDAO;
import com.udacity.jdnd.course3.critter.user.EmployeeDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Transactional
@Service
public class EmployeeService {
    @Autowired
    private EmployeeDAO employeeDAO;

    public EmployeeDTO addEmployee(EmployeeDTO employeeDTO) {
        if (Objects.isNull(employeeDTO))
            throw new RuntimeException();

        Long newId = employeeDAO.createNewEmployee(employeeDTO);
        employeeDTO.setId(newId);
        return getEmployeeByID(newId);
    }

    public EmployeeDTO getEmployeeByID(Long employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    public void updateEmployeeDaysAvailable(Set<DayOfWeek> daysAvailable, Long employeeId) {
        employeeDAO.updateEmployeeDaysAvailable(daysAvailable, employeeId);
    }

    public List<EmployeeDTO> getEmployeeByDaysAvailableAndSkill(EmployeeRequestDTO employeeRequestDTO) {
        return employeeDAO.getEmployeeByDaysAvailableAndSkill(employeeRequestDTO);
    }
}
