package com.udacity.jdnd.course3.critter.data;

import com.udacity.jdnd.course3.critter.user.CustomerDTO;
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
import java.util.List;
import java.util.Objects;

@Repository
public class CustomerDAO {
    private static final String SELECT_CUSTOMERS = "select * from customer order by id";
    private static final String SELECT_CUSTOMER_BY_PET_ID = "select * from customer c inner join customer_pet cp on cp.customer_id = c.id where cp.pet_id = :petId";
    private static final String SELECT_CUSTOMER_BY_ID = "select * from customer where id = :customerId";
    private static final String SELECT_PET_IDS_BY_CUSTOMER_ID = "select * from customer_pet where customer_id = :customerId";
    private static final String INSERT_CUSTOMER = "insert into customer (name, phone_number, notes) values (:name, :phoneNumber, :notes)";
    private static final String INSERT_CUSTOMER_PETS = "insert into customer_pet (customer_id, pet_id) values (:customerId, :petId)";
    private static final String DELETE_CUSTOMER_PET_BY_CUSTOMER_ID = "delete customer_pet where customer_id = :customerId";

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    public CustomerDTO getCustomerByPetId(Long petId) {
        CustomerDTO customerDTO = jdbcTemplate.queryForObject(SELECT_CUSTOMER_BY_PET_ID,
                new MapSqlParameterSource().addValue("petId", petId),
                new BeanPropertyRowMapper<>(CustomerDTO.class)
        );
        List<Long> petIdList = jdbcTemplate.query(
                SELECT_PET_IDS_BY_CUSTOMER_ID,
                new MapSqlParameterSource().addValue("customerId", customerDTO.getId()),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getLong(2); // petId is the second column of customer_pet table
                    }
                }
        );
        customerDTO.setPetIds(petIdList);
        return customerDTO;
    }

    public List<CustomerDTO> getAllCustomer() {
        List<CustomerDTO> customerDTOList = jdbcTemplate.query(SELECT_CUSTOMERS,
                new BeanPropertyRowMapper<>(CustomerDTO.class)
        );
        // get petIds for every customer
        customerDTOList.forEach(customerDTO -> {
                    List<Long> petIdList = jdbcTemplate.query(
                            SELECT_PET_IDS_BY_CUSTOMER_ID,
                            new MapSqlParameterSource().addValue("customerId", customerDTO.getId()),
                            new RowMapper<Long>() {
                                @Override
                                public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                                    return resultSet.getLong(2); // petId is the second column of customer_pet table
                                }
                            }
                    );
                    customerDTO.setPetIds(petIdList);
                }
        );

        return customerDTOList;
    }

    public CustomerDTO getCustomerById(Long customerId) {
        CustomerDTO customerDTO = jdbcTemplate.queryForObject(SELECT_CUSTOMER_BY_ID,
                new MapSqlParameterSource().addValue("customerId", customerId),
                new BeanPropertyRowMapper<>(CustomerDTO.class)
        );
        // get petIds for customer
        List<Long> petIdList = jdbcTemplate.query(
                SELECT_PET_IDS_BY_CUSTOMER_ID,
                new MapSqlParameterSource().addValue("customerId", customerDTO.getId()),
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getLong(2); // petId is the second column of customer_pet table
                    }
                }
        );
        customerDTO.setPetIds(petIdList);


        return customerDTO;
    }

    public void updatePetIdsForCustomer(Long customerId, List<Long> newPetIds) {
        jdbcTemplate.update(
                DELETE_CUSTOMER_PET_BY_CUSTOMER_ID,
                new MapSqlParameterSource().addValue("customerId", customerId)
        );

        newPetIds.forEach(petId -> {
            jdbcTemplate.update(
                    INSERT_CUSTOMER_PETS,
                    new MapSqlParameterSource()
                            .addValue("customerId", customerId)
                            .addValue("petId", petId)
            );
        });
    }

    public Long createNewCustomer(CustomerDTO customerDTO) {
        // Add new customer basic info
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(
                INSERT_CUSTOMER,
                new MapSqlParameterSource()
                        .addValue("name", customerDTO.getName())
                        .addValue("phoneNumber", customerDTO.getPhoneNumber())
                        .addValue("notes", customerDTO.getNotes()),
                key
        );
        Long newId = Objects.requireNonNull(key.getKey()).longValue();
        // check pet list and add to customer_pet table
        if (!Objects.isNull(customerDTO.getPetIds()) && !customerDTO.getPetIds().isEmpty()) {
            customerDTO.getPetIds().forEach(petId -> {
                jdbcTemplate.update(
                        INSERT_CUSTOMER_PETS,
                        new MapSqlParameterSource()
                                .addValue("customerId", newId)
                                .addValue("petId", petId)
                );
            });
        }
        return newId;
    }
}

