package com.werdnx.otus.socialnetwork.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String interests;
    private String city;
    private String passwordHash;


    @Override
    public String toString() {
        return "User{" +
                "city='" + city + '\'' +
                ", interests='" + interests + '\'' +
                ", gender='" + gender + '\'' +
                ", birthDate=" + birthDate +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", id=" + id +
                '}';
    }
}
