package com.phegon.FoodApp.auth_users.services;

import com.phegon.FoodApp.auth_users.dtos.UserDto;
import com.phegon.FoodApp.auth_users.entity.User;
import com.phegon.FoodApp.auth_users.repository.UserRepository;
import com.phegon.FoodApp.aws.AWSS3Service;
import com.phegon.FoodApp.email_notification.services.NotificationService;
import com.phegon.FoodApp.exceptions.NotFoundException;
import com.phegon.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final AWSS3Service awss3Service;


    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found "));
    }

    @Override
    public Response<List<UserDto>> getAllUsers() {

        List<User> userList = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UserDto> userDTOs = modelMapper.map(userList, new TypeToken<List<UserDto>>() {}.getType());

        return Response.<List<UserDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrieved successfully")
                .data(userDTOs)
                .build();
    }

    @Override
    public Response<UserDto> getOwnAccountDetails() {
        log.info("Fetching own account details for user: {}", getCurrentLoggedInUser().getEmail());
        User user = getCurrentLoggedInUser();
        UserDto userDTO = modelMapper.map(user, UserDto.class);

        return Response.<UserDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Own account details retrieved successfully")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<?> updateOwnAccount() {
        return null;
    }

    @Override
    public Response<?> deactivateOwnAccount() {
        return null;
    }
}
