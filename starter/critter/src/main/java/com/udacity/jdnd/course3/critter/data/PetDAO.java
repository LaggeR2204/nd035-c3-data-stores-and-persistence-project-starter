package com.udacity.jdnd.course3.critter.data;

import com.udacity.jdnd.course3.critter.pet.PetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PetDAO {
    private static final String SELECT_PET_BY_ID = "select * from pet where id = :id";
    private static final String SELECT_PETS = "select * from pet order by id";
    private static final String SELECT_PET_BY_OWNER_ID = "select * from pet where owner_id = :ownerId";
    private static final String INSERT_PET = "insert into pet (name, type, owner_id, birthdate, notes) values (:name, :type, :ownerId, :birthDate, :notes)";
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    public PetDTO getPetById(Long petId) {
        return jdbcTemplate.queryForObject(SELECT_PET_BY_ID,
                new MapSqlParameterSource().addValue("id", petId),
                new BeanPropertyRowMapper<>(PetDTO.class)
        );
    }

    public List<PetDTO> getPetByOwnerId(Long ownerId) {
        return jdbcTemplate.query(SELECT_PET_BY_OWNER_ID,
                new MapSqlParameterSource().addValue("ownerId", ownerId),
                new BeanPropertyRowMapper<>(PetDTO.class)
        );
    }

    public List<PetDTO> getAllPet() {
        return jdbcTemplate.query(SELECT_PETS,
                new BeanPropertyRowMapper<>(PetDTO.class)
        );
    }

    public Long createNewPet(PetDTO petDTO) {
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(
                INSERT_PET,
                new MapSqlParameterSource()
                        .addValue("name", petDTO.getName())
                        .addValue("type", petDTO.getType().toString())
                        .addValue("ownerId", petDTO.getOwnerId())
                        .addValue("birthDate", petDTO.getBirthDate())
                        .addValue("notes", petDTO.getNotes()),
                key
        );
        return Objects.requireNonNull(key.getKey()).longValue();
    }
}
