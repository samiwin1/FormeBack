package tn.esprit.forme.certificationservice.infrastructure.userdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service

public class UserDbLookupService {

    @Value("${app.userdb.url:jdbc:mysql://localhost:3306/forme?useUnicode=true&createDatabaseIfNotExist=true&useLegacyDatetimeCode=false&serverTimezone=UTC}")
    private String userDbUrl;

    @Value("${app.userdb.username:root}")
    private String userDbUsername;

    @Value("${app.userdb.password:}")
    private String userDbPassword;

    public Map<Long, UserRow> findUsersByIds(Collection<Long> ids) {
        Map<Long, UserRow> rowsById = new LinkedHashMap<>();
        List<Long> uniqueIds = ids == null ? List.of() : ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (uniqueIds.isEmpty()) {
            return rowsById;
        }

        String placeholders = String.join(",", uniqueIds.stream().map(id -> "?").toList());
        String sql = "select id, first_name, last_name, email, profession from users where id in (" + placeholders + ")";

        try (Connection cn = DriverManager.getConnection(userDbUrl, userDbUsername, userDbPassword);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int index = 1;
            for (Long id : uniqueIds) {
                ps.setLong(index++, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String firstName = trimToNull(rs.getString("first_name"));
                    String lastName = trimToNull(rs.getString("last_name"));
                    String email = trimToNull(rs.getString("email"));
                    String profession = trimToNull(rs.getString("profession"));
                    rowsById.put(id, new UserRow(id, firstName, lastName, email, profession));
                }
            }
        } catch (Exception ignored) {
            // best-effort fallback only
        }

        return rowsById;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record UserRow(Long id, String firstName, String lastName, String email, String profession) {
    }
}
