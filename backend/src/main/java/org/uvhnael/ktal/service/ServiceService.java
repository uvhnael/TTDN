package org.uvhnael.ktal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.uvhnael.ktal.model.Service;
import org.uvhnael.ktal.repository.ServiceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    public Optional<Service> findById(Long id) {
        return serviceRepository.findById(id);
    }

    public int create(Service service) {
        return serviceRepository.save(service);
    }

    public int update(Service service) {
        return serviceRepository.update(service);
    }

    public int delete(Long id) {
        return serviceRepository.deleteById(id);
    }
}
