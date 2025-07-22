package com.phegon.FoodApp.auth_users.services;

import com.phegon.FoodApp.auth_users.dtos.UserDto;
import com.phegon.FoodApp.auth_users.entity.User;
import com.phegon.FoodApp.response.Response;

import java.util.List;

public interface UserService {

    User getCurrentLoggedInUser();
    Response<List<UserDto>> getAllUsers();
    Response<UserDto> getOwnAccountDetails();

    Response<?> updateOwnAccount();
    Response<?> deactivateOwnAccount();
}
