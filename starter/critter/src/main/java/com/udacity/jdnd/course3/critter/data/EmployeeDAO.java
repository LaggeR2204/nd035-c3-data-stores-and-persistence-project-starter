package com.udacity.jdnd.course3.critter.data;

import com.udacity.jdnd.course3.critter.user.EmployeeDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeRequestDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Repository
public class EmployeeDAO {
    private static final String SELECT_EMPLOYEE_BY_ID = "select * from employee where id = :employeeId";
    private static final String DELETE_DAYS_AVAILABLE_BY_EMPLOYEE_ID = "delete from employee_days_available where employee_id = :employeeId";
    private static final String SELECT_SKILL_BY_EMPLOYEE_ID = "select * from skill s inner join employee_skill es on es.skill_id = s.skill_id where es.employee_id = :employeeId";
    private static final String SELECT_DAYS_OF_WEEK_BY_EMPLOYEE_ID = "select * from employee_days_available where employee_id = :employeeId";
    private static final String INSERT_EMPLOYEE = "insert into employee (name) values (:name)";
    private static final String INSERT_EMPLOYEE_SKILLS = "insert into employee_skill (employee_id, skill_id) values (:employeeId, :skillId)";
    private static final String INSERT_EMPLOYEE_DAYS_AVAILABLE = "insert into employee_days_available (employee_id, day_of_week) values (:employeeId, :dayOfWeek)";
    private static final String SELECT_SKILL_BY_NAME = "select * from skill where skill_name = :skill";
    private static final String SELECT_EMPLOYEE_BY_DAYS_AVAILABLE_AND_SKILL = "select e.id, count(e.id) from employee e inner join employee_days_available ed on ed.employee_id = e.id inner join employee_skill es on es.employee_id = e.id inner join skill s on s.skill_id = es.skill_id where ed.day_of_week = :dayOfWeek and s.skill_name in (:skills) group by e.id";

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeDTO getEmployeeById(Long employeeId) {
        EmployeeDTO employeeDTO = jdbcTemplate.queryForObject(SELECT_EMPLOYEE_BY_ID,
                new MapSqlParameterSource().addValue("employeeId", employeeId),
                new BeanPropertyRowMapper<>(EmployeeDTO.class)
        );
        if (!Objects.isNull(employeeDTO)) {
            // get skills for employee
            List<EmployeeSkill> skillList = jdbcTemplate.query(
                    SELECT_SKILL_BY_EMPLOYEE_ID,
                    new MapSqlParameterSource().addValue("employeeId", employeeDTO.getId()),
                    new RowMapper<EmployeeSkill>() {
                        @Override
                        public EmployeeSkill mapRow(ResultSet resultSet, int i) throws SQLException {
                            return EmployeeSkill.valueOf(resultSet.getString(2)); // skill_name is the second column of skill table
                        }
                    }
            );
            employeeDTO.setSkills(skillList.isEmpty() ? null : new HashSet<EmployeeSkill>(skillList));
            // get daysAvailable for employee
            List<DayOfWeek> dayOfWeekList = jdbcTemplate.query(
                    SELECT_DAYS_OF_WEEK_BY_EMPLOYEE_ID,
                    new MapSqlParameterSource().addValue("employeeId", employeeDTO.getId()),
                    new RowMapper<DayOfWeek>() {
                        @Override
                        public DayOfWeek mapRow(ResultSet resultSet, int i) throws SQLException {
                            return DayOfWeek.valueOf(resultSet.getString(2)); // dayOfWeek is the second column of employee_day_available table
                        }
                    }
            );
            employeeDTO.setDaysAvailable(dayOfWeekList.isEmpty() ? null : new HashSet<DayOfWeek>(dayOfWeekList));
        }

        return employeeDTO;
    }

    public List<EmployeeDTO> getEmployeeByDaysAvailableAndSkill(EmployeeRequestDTO employeeRequestDTO) {
        List<Long> employeeIdList = jdbcTemplate.query(
                SELECT_EMPLOYEE_BY_DAYS_AVAILABLE_AND_SKILL,
                new MapSqlParameterSource()
                        .addValue("dayOfWeek", LocalDate.parse(employeeRequestDTO.getDate().toString()).getDayOfWeek().toString())
                        .addValue("skills", employeeRequestDTO.getSkills().stream().map(Enum::toString).collect(Collectors.toSet())),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                        if (resultSet.getInt(2) >= employeeRequestDTO.getSkills().size()) {
                            return resultSet.getLong(1);
                        } else {
                            return 0L;
                        }
                    }
                }
        );
        List<EmployeeDTO> employeeDTOList = new ArrayList<>();
        employeeIdList.forEach(employeeId -> {
            if (employeeId != 0) {
                employeeDTOList.add(getEmployeeById(employeeId));
            }
        });

        return employeeDTOList;
    }

    public Long createNewEmployee(EmployeeDTO employeeDTO) {
        // Add new employee basic info
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(
                INSERT_EMPLOYEE,
                new MapSqlParameterSource()
                        .addValue("name", employeeDTO.getName()),
                key
        );
        Long newId = Objects.requireNonNull(key.getKey()).longValue();
        // check skill list and add to employee_skill table
        if (!Objects.isNull(employeeDTO.getSkills()) && !employeeDTO.getSkills().isEmpty()) {
            employeeDTO.getSkills().forEach(skill -> {
                Long skillId = jdbcTemplate.queryForObject(
                        SELECT_SKILL_BY_NAME,
                        new MapSqlParameterSource().addValue("skill", skill.toString()),
                        new RowMapper<Long>() {
                            @Override
                            public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                return resultSet.getLong(1); // skillId is the first column of skill table
                            }
                        }
                );
                jdbcTemplate.update(
                        INSERT_EMPLOYEE_SKILLS,
                        new MapSqlParameterSource()
                                .addValue("employeeId", newId)
                                .addValue("skillId", skillId)
                );
            });
        }
        // check daysAvailable list and add to employee_days_available table
        if (!Objects.isNull(employeeDTO.getDaysAvailable()) && !employeeDTO.getDaysAvailable().isEmpty()) {
            employeeDTO.getDaysAvailable().forEach(dayOfWeek -> {
                jdbcTemplate.update(
                        INSERT_EMPLOYEE_DAYS_AVAILABLE,
                        new MapSqlParameterSource()
                                .addValue("employeeId", newId)
                                .addValue("dayOfWeek", dayOfWeek.toString())
                );
            });
        }
        return newId;
    }

    public void updateEmployeeDaysAvailable(Set<DayOfWeek> daysAvailable, Long employeeId) {
        // Delete existing daysAvailable
        jdbcTemplate.update(
                DELETE_DAYS_AVAILABLE_BY_EMPLOYEE_ID,
                new MapSqlParameterSource().addValue("employeeId", employeeId)
        );

        // Add new daysAvailable
        daysAvailable.forEach(dayOfWeek -> {
            jdbcTemplate.update(
                    INSERT_EMPLOYEE_DAYS_AVAILABLE,
                    new MapSqlParameterSource()
                            .addValue("employeeId", employeeId)
                            .addValue("dayOfWeek", dayOfWeek.toString())
            );
        });
    }
}

