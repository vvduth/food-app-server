package com.phegon.FoodApp.auth_users.services;

import com.phegon.FoodApp.auth_users.dtos.UserDto;
import com.phegon.FoodApp.auth_users.entity.User;
import com.phegon.FoodApp.auth_users.repository.UserRepository;
import com.phegon.FoodApp.aws.AWSS3Service;
import com.phegon.FoodApp.email_notification.dtos.NotificationDTO;
import com.phegon.FoodApp.email_notification.services.NotificationService;
import com.phegon.FoodApp.exceptions.BadRequestException;
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
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

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
    public Response<?> updateOwnAccount(UserDto userDTO) {
        log.info("Updating own account for user: {}", getCurrentLoggedInUser().getEmail());
        // fetch current logged-in user
        User user = getCurrentLoggedInUser();

        String profileUrl = user.getProfileUrl();

        MultipartFile imageFile = userDTO.getImageFile();

        // check if new image file was provided
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete the old image from S3 if it exists
            if (profileUrl != null && !profileUrl.isEmpty()) {
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);
                awss3Service.deleteFile("profile/" + keyName);

                log.info("Deleted old profile image from s3");
            }
            //upload new image
            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.uploadFile("profile/" + imageName, imageFile);

            user.setProfileUrl(newImageUrl.toString());
        }

        // update user details
        if ( userDTO.getName() != null){
            user.setName(userDTO.getName());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            // Check if the new email is already taken
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }


        // save updated user
        userRepository.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Own account updated successfully")
                .data(modelMapper.map(user, UserDto.class))
                .build();
    }

    @Override
    public Response<?> deactivateOwnAccount() {
        log.info("Deactivating own account for user: {}", getCurrentLoggedInUser().getEmail());
        User user = getCurrentLoggedInUser();

        // deactivate user
        user.setActive(false);
        userRepository.save(user);

        // send notification email
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Account Deactivation")
                .body("Dear " + user.getName() + ",\n\n" +
                        "Your account has been successfully deactivated. If this was a mistake, please contact support.\n\n" +
                        "Best regards,\n" +
                        "FoodApp Team")
                .build();
        notificationService.sendEmail(notificationDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Own account deactivated successfully")
                .build();
    }
}
