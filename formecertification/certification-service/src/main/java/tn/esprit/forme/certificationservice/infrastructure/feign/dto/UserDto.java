package tn.esprit.forme.certificationservice.infrastructure.feign.dto;

import lombok.Data;

@Data

public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profession;

    public String getFullName() {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (first + " " + last).trim();
    }
}
