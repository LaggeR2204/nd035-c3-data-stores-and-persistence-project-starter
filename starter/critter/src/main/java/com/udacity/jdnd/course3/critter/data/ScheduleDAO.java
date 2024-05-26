package com.udacity.jdnd.course3.critter.data;

import com.udacity.jdnd.course3.critter.schedule.ScheduleDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeSkill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class ScheduleDAO {
    private static final String SELECT_SCHEDULES = "select * from schedule order by date";
    private static final String SELECT_SCHEDULES_BY_ID = "select * from schedule where id = :scheduleId";
    private static final String SELECT_SCHEDULES_BY_PET_ID = "select * from schedule s inner join schedule_pet sp on sp.schedule_id = s.id where sp.pet_id = :petId";
    private static final String SELECT_SCHEDULES_BY_EMPLOYEE_ID = "select * from schedule s inner join schedule_employee se on se.schedule_id = s.id where se.employee_id = :employeeId";
    private static final String SELECT_PET_IDS_BY_SCHEDULE_ID = "select * from schedule_pet where schedule_id = :scheduleId";
    private static final String SELECT_SKILLS_BY_SCHEDULE_ID = "select * from skill s inner join schedule_activity sa on s.skill_id = sa.skill_id where schedule_id = :scheduleId";
    private static final String SELECT_EMPLOYEE_IDS_BY_SCHEDULE_ID = "select * from schedule_employee where schedule_id = :scheduleId";
    private static final String INSERT_SCHEDULE = "insert into schedule (date) values (:date)";
    private static final String INSERT_SCHEDULE_EMPLOYEE = "insert into schedule_employee (schedule_id, employee_id) values (:scheduleId, :employeeId)";
    private static final String INSERT_SCHEDULE_PET = "insert into schedule_pet (schedule_id, pet_id) values (:scheduleId, :petId)";
    private static final String INSERT_SCHEDULE_SKILL = "insert into schedule_activity (schedule_id, skill_id) values (:scheduleId, :skillId)";
    private static final String SELECT_SKILL_BY_NAME = "select * from skill where skill_name = :skill";
    private static final String SELECT_PET_IDS_BY_CUSTOMER_ID = "select * from customer_pet where customer_id = :customerId";


    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public List<ScheduleDTO> getAllSchedules() {
        List<ScheduleDTO> scheduleDTOList = jdbcTemplate.query(SELECT_SCHEDULES,
                new BeanPropertyRowMapper<>(ScheduleDTO.class)
        );
        // get petIds for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<Long> petIdList = jdbcTemplate.query(
                            SELECT_PET_IDS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // petId is the second column of schedule_pet table
                                }
                            }
                    );
                    scheduleDTO.setPetIds(petIdList);
                }
        );

        // get employeeIds for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<Long> employeeIdList = jdbcTemplate.query(
                            SELECT_EMPLOYEE_IDS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // employeeId is the second column of schedule_employee table
                                }
                            }
                    );
                    scheduleDTO.setEmployeeIds(employeeIdList);
                }
        );

        // get skill list for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<EmployeeSkill> skillNameList = jdbcTemplate.query(
                            SELECT_SKILLS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<EmployeeSkill>() {
                                @Override
                                public EmployeeSkill mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return EmployeeSkill.valueOf(resultSet.getString(2)); // skill_name is the second column of skill table
                                }
                            }
                    );
                    scheduleDTO.setActivities(new HashSet<EmployeeSkill>(skillNameList));
                }
        );

        return scheduleDTOList;
    }

    public ScheduleDTO getScheduleById(Long scheduleId) {
        ScheduleDTO scheduleDTO = jdbcTemplate.queryForObject(SELECT_SCHEDULES_BY_ID,
                new MapSqlParameterSource().addValue("scheduleId", scheduleId),
                new BeanPropertyRowMapper<>(ScheduleDTO.class)
        );
        // get petIds for schedule
        List<Long> petIdList = jdbcTemplate.query(
                SELECT_PET_IDS_BY_SCHEDULE_ID,
                new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getLong(2); // petId is the second column of schedule_pet table
                    }
                }
        );
        scheduleDTO.setPetIds(petIdList);

        // get employeeIds for schedule
        List<Long> employeeIdList = jdbcTemplate.query(
                SELECT_EMPLOYEE_IDS_BY_SCHEDULE_ID,
                new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getLong(2); // employeeId is the second column of schedule_employee table
                    }
                }
        );
        scheduleDTO.setEmployeeIds(employeeIdList);

        // get skill list for schedule
        List<EmployeeSkill> skillNameList = jdbcTemplate.query(
                SELECT_SKILLS_BY_SCHEDULE_ID,
                new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                new RowMapper<EmployeeSkill>() {
                    @Override
                    public EmployeeSkill mapRow(ResultSet resultSet, int i) throws SQLException {
                        return EmployeeSkill.valueOf(resultSet.getString(2)); // skill_name is the second column of skill table
                    }
                }
        );
        scheduleDTO.setActivities(new HashSet<EmployeeSkill>(skillNameList));

        return scheduleDTO;
    }

    public List<ScheduleDTO> getScheduleByPetId(Long petId) {
        List<ScheduleDTO> scheduleDTOList = jdbcTemplate.query(SELECT_SCHEDULES_BY_PET_ID,
                new MapSqlParameterSource().addValue("petId", petId),
                new BeanPropertyRowMapper<>(ScheduleDTO.class)
        );
        // get petIds for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<Long> petIdList = jdbcTemplate.query(
                            SELECT_PET_IDS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // petId is the second column of schedule_pet table
                                }
                            }
                    );
                    scheduleDTO.setPetIds(petIdList);
                }
        );

        // get employeeIds for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<Long> employeeIdList = jdbcTemplate.query(
                            SELECT_EMPLOYEE_IDS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // employeeId is the second column of schedule_employee table
                                }
                            }
                    );
                    scheduleDTO.setEmployeeIds(employeeIdList);
                }
        );

        // get skill list for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<EmployeeSkill> skillNameList = jdbcTemplate.query(
                            SELECT_SKILLS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<EmployeeSkill>() {
                                @Override
                                public EmployeeSkill mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return EmployeeSkill.valueOf(resultSet.getString(2)); // skill_name is the second column of skill table
                                }
                            }
                    );
                    scheduleDTO.setActivities(new HashSet<EmployeeSkill>(skillNameList));
                }
        );

        return scheduleDTOList;
    }

    public List<ScheduleDTO> getScheduleByEmployeeId(Long employeeId) {
        List<ScheduleDTO> scheduleDTOList = jdbcTemplate.query(SELECT_SCHEDULES_BY_EMPLOYEE_ID,
                new MapSqlParameterSource().addValue("employeeId", employeeId),
                new BeanPropertyRowMapper<>(ScheduleDTO.class)
        );
        // get petIds for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<Long> petIdList = jdbcTemplate.query(
                            SELECT_PET_IDS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // petId is the second column of schedule_pet table
                                }
                            }
                    );
                    scheduleDTO.setPetIds(petIdList);
                }
        );

        // get employeeIds for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<Long> employeeIdList = jdbcTemplate.query(
                            SELECT_EMPLOYEE_IDS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // employeeId is the second column of schedule_employee table
                                }
                            }
                    );
                    scheduleDTO.setEmployeeIds(employeeIdList);
                }
        );

        // get skill list for every schedule
        scheduleDTOList.forEach(scheduleDTO -> {
                    List<EmployeeSkill> skillNameList = jdbcTemplate.query(
                            SELECT_SKILLS_BY_SCHEDULE_ID,
                            new MapSqlParameterSource().addValue("scheduleId", scheduleDTO.getId()),
                            new RowMapper<EmployeeSkill>() {
                                @Override
                                public EmployeeSkill mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return EmployeeSkill.valueOf(resultSet.getString(2)); // skill_name is the second column of skill table
                                }
                            }
                    );
                    scheduleDTO.setActivities(new HashSet<EmployeeSkill>(skillNameList));
                }
        );

        return scheduleDTOList;
    }

    public List<ScheduleDTO> getScheduleByCustomerId(Long customerId) {
        List<Long> petIdList = jdbcTemplate.query(
                SELECT_PET_IDS_BY_CUSTOMER_ID,
                new MapSqlParameterSource().addValue("customerId", customerId),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getLong(2); // petId is the second column of customer_pet table
                    }
                }
        );
        List<ScheduleDTO> scheduleDTOList = new ArrayList<>();

        petIdList.forEach(petId -> {
            scheduleDTOList.addAll(getScheduleByPetId(petId));
        });

        return scheduleDTOList.stream().filter(distinctByKey(ScheduleDTO::getId)).collect(Collectors.toList());
    }

    public Long createNewSchedule(ScheduleDTO scheduleDTO) {
        // Add new schedule basic info
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(
                INSERT_SCHEDULE,
                new MapSqlParameterSource()
                        .addValue("date", scheduleDTO.getDate()),
                key
        );
        Long newId = Objects.requireNonNull(key.getKey()).longValue();
        // check employeeId list and add to schedule_employee table
        if (!Objects.isNull(scheduleDTO.getEmployeeIds()) && !scheduleDTO.getEmployeeIds().isEmpty()) {
            scheduleDTO.getEmployeeIds().forEach(employeeId -> {
                jdbcTemplate.update(
                        INSERT_SCHEDULE_EMPLOYEE,
                        new MapSqlParameterSource()
                                .addValue("scheduleId", newId)
                                .addValue("employeeId", employeeId)
                );
            });
        }
        // check petId list and add to schedule_pet table
        if (!Objects.isNull(scheduleDTO.getPetIds()) && !scheduleDTO.getPetIds().isEmpty()) {
            scheduleDTO.getPetIds().forEach(petId -> {
                jdbcTemplate.update(
                        INSERT_SCHEDULE_PET,
                        new MapSqlParameterSource()
                                .addValue("scheduleId", newId)
                                .addValue("petId", petId)
                );
            });
        }
        // check activity list and add to schedule_activity table
        if (!Objects.isNull(scheduleDTO.getActivities()) && !scheduleDTO.getActivities().isEmpty()) {
            scheduleDTO.getActivities().forEach(skill -> {
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
                        INSERT_SCHEDULE_SKILL,
                        new MapSqlParameterSource()
                                .addValue("scheduleId", newId)
                                .addValue("skillId", skillId)
                );
            });
        }
        return newId;
    }
}

