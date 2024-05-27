package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.data.CustomerDAO;
import com.udacity.jdnd.course3.critter.data.PetDAO;
import com.udacity.jdnd.course3.critter.pet.PetDTO;
import com.udacity.jdnd.course3.critter.user.CustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PetService {
    @Autowired
    private PetDAO petDAO;

    @Autowired
    private CustomerDAO customerDAO;

    public PetDTO addPet(PetDTO petDTO) {
        if (Objects.isNull(petDTO))
            throw new RuntimeException();

        Long newId = petDAO.createNewPet(petDTO);
        petDTO.setId(newId);

        try {
            CustomerDTO owner = customerDAO.getCustomerById(petDTO.getOwnerId());
            if (!Objects.isNull(owner) && !owner.getPetIds().contains(newId)) {
                List<Long> newPetIds = owner.getPetIds();
                newPetIds.add(petDTO.getId());
                customerDAO.updatePetIdsForCustomer(owner.getId(), newPetIds);
            }
        } catch (RuntimeException e) {

        }

        return getPetById(newId);
    }

    public PetDTO getPetById(Long id) {
        return petDAO.getPetById(id);
    }

    public List<PetDTO> getPets() {
        return petDAO.getAllPet();
    }

    public List<PetDTO> getPetByOwnerId(Long id) {
        return petDAO.getPetByOwnerId(id);
    }

}
