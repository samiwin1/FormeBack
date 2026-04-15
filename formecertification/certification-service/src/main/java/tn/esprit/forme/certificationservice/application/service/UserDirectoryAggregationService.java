package tn.esprit.forme.certificationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forme.certificationservice.application.dto.dashboard.UserDirectoryEntryDto;
import tn.esprit.forme.certificationservice.domain.repository.IssuedCertificationRepository;
import tn.esprit.forme.certificationservice.domain.repository.OralExamAssignmentRepository;
import tn.esprit.forme.certificationservice.infrastructure.feign.UserClient;
import tn.esprit.forme.certificationservice.infrastructure.feign.UserDirectoryClient;
import tn.esprit.forme.certificationservice.infrastructure.feign.dto.UserDto;
import tn.esprit.forme.certificationservice.infrastructure.userdb.UserDbLookupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor

public class UserDirectoryAggregationService {

    private final OralExamAssignmentRepository assignmentRepository;
    private final IssuedCertificationRepository issuedCertificationRepository;
    private final UserDirectoryClient userDirectoryClient;
    private final UserClient userClient;
    private final UserDbLookupService userDbLookupService;

    @Transactional(readOnly = true)
    public List<UserDirectoryEntryDto> getAdminDirectory() {
        Set<Long> userIds = new TreeSet<>();

        assignmentRepository.findAll().forEach(assignment -> {
            if (assignment.getLearnerId() != null) {
                userIds.add(assignment.getLearnerId());
            }
            if (assignment.getOralSession() != null && assignment.getOralSession().getEvaluatorId() != null) {
                userIds.add(assignment.getOralSession().getEvaluatorId());
            }
        });

        issuedCertificationRepository.findAll().forEach(issued -> {
            if (issued.getLearnerId() != null) {
                userIds.add(issued.getLearnerId());
            }
        });

        Map<Long, UserDto> usersById = fetchUsersById(userIds);
        return userIds.stream()
                .map(id -> resolveUserEntry(id, usersById))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, String> resolveDisplayNames(List<Long> ids) {
        Map<Long, String> result = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }

        Map<Long, UserDto> usersById = fetchUsersById(ids);
        ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> result.put(id, resolveDisplayName(id, usersById)));

        return result;
    }

    @Transactional(readOnly = true)
    public UserDirectoryEntryDto getById(Long userId) {
        Map<Long, UserDto> usersById = fetchUsersById(List.of(userId));
        return resolveUserEntry(userId, usersById);
    }

    private UserDirectoryEntryDto resolveUserEntry(Long userId, Map<Long, UserDto> usersById) {
        UserDto user = usersById.get(userId);
        String firstName = user != null ? trimToNull(user.getFirstName()) : null;
        String lastName = user != null ? trimToNull(user.getLastName()) : null;
        String profession = user != null ? trimToNull(user.getProfession()) : null;
        String displayName = buildDisplayName(firstName, lastName, userId);
        return new UserDirectoryEntryDto(userId, firstName, lastName, profession, displayName);
    }

    private String resolveDisplayName(Long userId, Map<Long, UserDto> usersById) {
        UserDto user = usersById.get(userId);
        String firstName = user != null ? trimToNull(user.getFirstName()) : null;
        String lastName = user != null ? trimToNull(user.getLastName()) : null;
        return buildDisplayName(firstName, lastName, userId);
    }

    private Map<Long, UserDto> fetchUsersById(Collection<Long> requestedIds) {
        Map<Long, UserDto> usersById = new LinkedHashMap<>();
        List<UserDto> rows = new ArrayList<>();

        rows.addAll(fetchSafely(userDirectoryClient::listAdmins));
        rows.addAll(fetchSafely(userDirectoryClient::listSuperAdmins));

        for (UserDto user : rows) {
            if (user == null || user.getId() == null) {
                continue;
            }
            usersById.putIfAbsent(user.getId(), user);
        }

        Set<Long> discoveredIds = new TreeSet<>();
        assignmentRepository.findAll().forEach(assignment -> {
            if (assignment.getLearnerId() != null) {
                discoveredIds.add(assignment.getLearnerId());
            }
            if (assignment.getOralSession() != null && assignment.getOralSession().getEvaluatorId() != null) {
                discoveredIds.add(assignment.getOralSession().getEvaluatorId());
            }
        });
        issuedCertificationRepository.findAll().forEach(issued -> {
            if (issued.getLearnerId() != null) {
                discoveredIds.add(issued.getLearnerId());
            }
        });

        Set<Long> lookupScope = new TreeSet<>(discoveredIds);
        if (requestedIds != null) {
            requestedIds.stream()
                    .filter(Objects::nonNull)
                    .forEach(lookupScope::add);
        }

        Set<Long> missingIds = new TreeSet<>(lookupScope);
        missingIds.removeAll(usersById.keySet());
        if (!missingIds.isEmpty()) {
            Map<Long, UserDbLookupService.UserRow> fromDb = userDbLookupService.findUsersByIds(missingIds);
            fromDb.values().forEach(row -> {
                UserDto dto = new UserDto();
                dto.setId(row.id());
                dto.setFirstName(row.firstName());
                dto.setLastName(row.lastName());
                dto.setEmail(row.email());
                dto.setProfession(row.profession());
                usersById.putIfAbsent(row.id(), dto);
            });
        }

        return usersById;
    }

    private Collection<UserDto> fetchSafely(SupplierWithException<List<UserDto>> supplier) {
        try {
            List<UserDto> rows = supplier.get();
            return rows == null ? List.of() : rows;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String buildDisplayName(String firstName, String lastName, Long userId) {
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        return fullName.isBlank() ? ("User #" + userId) : fullName;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    public String resolveUserName(Long userId) {
        if (userId == null) {
            return "Unknown User";
        }
        Map<Long, UserDto> usersById = fetchUsersById(List.of(userId));
        UserDto user = usersById.get(userId);
        String firstName = user != null ? trimToNull(user.getFirstName()) : null;
        String lastName = user != null ? trimToNull(user.getLastName()) : null;
        return buildDisplayName(firstName, lastName, userId);
    }

    public String resolveUserEmail(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            UserDto user = userClient.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                return user.getEmail();
            }
        } catch (Exception e) {
            // Try from database lookup
            Map<Long, UserDbLookupService.UserRow> fromDb = userDbLookupService.findUsersByIds(List.of(userId));
            UserDbLookupService.UserRow row = fromDb.get(userId);
            if (row != null && row.email() != null) {
                return row.email();
            }
        }
        return null;
    }
}
