package com.phegon.FoodApp.role.controller;


import com.phegon.FoodApp.response.Response;
import com.phegon.FoodApp.role.dtos.RoleDTO;
import com.phegon.FoodApp.role.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
//@PreAuthorize("hasAuthority('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Response<RoleDTO>> createRole(
            @RequestBody @Valid RoleDTO roleDTO
    ) {
        return ResponseEntity.ok(
                roleService.createRole(roleDTO)
        );
    }

    @PutMapping
    public ResponseEntity<Response<RoleDTO>> updateRole(
            @RequestBody @Valid RoleDTO roleDTO
    ) {
        return ResponseEntity.ok(
                roleService.updateRole(roleDTO)
        );
    }

    @GetMapping
    public ResponseEntity<Response<?>> getAllRoles() {
        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<?>> deleteRole(@PathVariable Long id) {
        return ResponseEntity.ok(
                roleService.deleteRole(id)
        );
    }
}
