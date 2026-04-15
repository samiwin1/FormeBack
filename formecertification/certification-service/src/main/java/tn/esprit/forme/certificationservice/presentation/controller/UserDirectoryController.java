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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<UserDirectoryEntryDto> adminDirectory() {
        try {
            return userDirectoryService.getAdminDirectory();
        } catch (Exception e) {
            return List.of();
        }
    }

    @GetMapping("/user-directory")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Map<Long, String> userDirectory(@RequestParam(name = "ids") List<Long> ids) {
        try {
            return userDirectoryService.resolveDisplayNames(ids);
        } catch (Exception e) {
            return Map.of();
        }
    }

    @GetMapping("/user-directory/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public UserDirectoryEntryDto userById(@PathVariable Long userId) {
        try {
            return userDirectoryService.getById(userId);
        } catch (Exception e) {
            return new UserDirectoryEntryDto(userId, null, null, null, "User #" + userId);
        }
    }
}
