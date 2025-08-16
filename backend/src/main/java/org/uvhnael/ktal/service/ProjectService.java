package org.uvhnael.ktal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uvhnael.ktal.model.Project;
import org.uvhnael.ktal.model.ProjectImage;
import org.uvhnael.ktal.repository.ProjectImageRepository;
import org.uvhnael.ktal.repository.ProjectRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();

    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public int create(Project project) {
        project.setCreatedAt(LocalDateTime.now().toString());
        project.setUpdatedAt(LocalDateTime.now().toString());
        return projectRepository.save(project);
    }

    public int update(Project project) {
        return projectRepository.update(project);
    }

    public int delete(Long id) {
        return projectRepository.deleteById(id);
    }

    public int deleteImage(Long id) {
        return projectImageRepository.deleteById(id);
    }

    public int createImage(ProjectImage image) {
        return projectImageRepository.save(image);
    }
}
