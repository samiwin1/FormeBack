package tn.esprit.forme.certificationservice.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.forme.certificationservice.application.dto.dashboard.UserDirectoryEntryDto;
import tn.esprit.forme.certificationservice.application.service.UserDirectoryAggregationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserDirectoryController {

    private final UserDirectoryAggregationService userDirectoryService;

    @GetMapping("/admin/user-directory")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDirectoryEntryDto> adminDirectory() {
        return userDirectoryService.getAdminDirectory();
    }

    @GetMapping("/user-directory")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<Long, String> userDirectory(@RequestParam(name = "ids") List<Long> ids) {
        return userDirectoryService.resolveDisplayNames(ids);
    }

    @GetMapping("/user-directory/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDirectoryEntryDto userById(@PathVariable Long userId) {
        return userDirectoryService.getById(userId);
    }
}
