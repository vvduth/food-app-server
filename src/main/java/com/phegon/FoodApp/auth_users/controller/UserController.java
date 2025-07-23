package com.phegon.FoodApp.auth_users.controller;

import com.phegon.FoodApp.auth_users.dtos.UserDto;
import com.phegon.FoodApp.auth_users.services.UserService;
import com.phegon.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private  final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ResponseEntity<Response<UserDto>> getOwnAccountDetails() {
        return ResponseEntity.ok(userService.getOwnAccountDetails());
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<?>> updateOwnAccount(
            @ModelAttribute  UserDto userDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) {
        userDTO.setImageFile(imageFile);
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));
    }

}
