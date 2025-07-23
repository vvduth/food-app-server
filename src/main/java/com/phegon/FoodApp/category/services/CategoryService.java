package com.phegon.FoodApp.category.services;

import com.phegon.FoodApp.category.dtos.CategoryDTO;
import com.phegon.FoodApp.response.Response;

import java.util.List;

public interface CategoryService {
    Response<CategoryDTO> addCategory(CategoryDTO categoryDTO);
    Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO);
    Response<CategoryDTO> getCategoryById(Long id);
    Response<?> deleteCategory(Long id);
    Response<List<CategoryDTO>> getAllCategories();
}
