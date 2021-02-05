package com.example.jwtdemo.service;

import com.example.jwtdemo.models.ERole;
import com.example.jwtdemo.models.Role;
import com.example.jwtdemo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    @Transactional
    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }
}
