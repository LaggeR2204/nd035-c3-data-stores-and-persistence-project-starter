package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.data.CustomerDAO;
import com.udacity.jdnd.course3.critter.user.CustomerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {
    @Autowired
    private CustomerDAO customerDAO;

    public CustomerDTO addCustomer(CustomerDTO customerDTO) {
        if (Objects.isNull(customerDTO))
            throw new RuntimeException();

        Long newId = customerDAO.createNewCustomer(customerDTO);
        customerDTO.setId(newId);
        return customerDAO.getCustomerById(newId);
    }

    public List<CustomerDTO> getCustomers() {
        return customerDAO.getAllCustomer();
    }

    public CustomerDTO getCustomerByPetId(Long petId) {
        return customerDAO.getCustomerByPetId(petId);
    }

}
