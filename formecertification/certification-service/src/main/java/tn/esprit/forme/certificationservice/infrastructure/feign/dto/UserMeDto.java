package tn.esprit.forme.certificationservice.infrastructure.feign.dto;

import lombok.Data;

import java.util.Set;

@Data

public class UserMeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profession;
    private Set<String> roles;

    public String getFullName() {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (first + " " + last).trim();
    }
}
