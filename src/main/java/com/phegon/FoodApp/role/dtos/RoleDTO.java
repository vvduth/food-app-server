package com.phegon.FoodApp.role.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Include this annotation to avoid null fields in JSON response
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any unknown properties in JSON
public class RoleDTO {
    private Long id; // Unique identifier for the role
    private String name; // Name of the role (e.g., ADMIN, USER, MODERATOR)

    // Additional fields can be added here as needed
}
