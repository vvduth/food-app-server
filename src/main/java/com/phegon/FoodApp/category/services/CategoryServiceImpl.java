package com.phegon.FoodApp.category.services;

import com.phegon.FoodApp.category.dtos.CategoryDTO;
import com.phegon.FoodApp.category.entity.Category;
import com.phegon.FoodApp.category.repository.CategoryRepository;
import com.phegon.FoodApp.exceptions.NotFoundException;
import com.phegon.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<CategoryDTO> addCategory(CategoryDTO categoryDTO) {
        log.info("Adding new category: {}", categoryDTO.getName());

        Category category = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.save(category);

        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category added successfully")
                .build();
    }

    @Override
    public Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO) {
        log.info("Updating category: {}", categoryDTO.getName());

        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new NotFoundException("category not found"));
        if (categoryDTO.getName() != null && !categoryDTO.getName().isEmpty()) {
            category.setName(categoryDTO.getName());
        }
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }

        categoryRepository.save(category);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category updated successfully")
                .data(modelMapper.map(category, CategoryDTO.class))
                .build();
    }

    @Override
    public Response<CategoryDTO> getCategoryById(Long id) {
        log.info("Fetching category by ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Category fetched successfully")
                .data(categoryDTO)
                .build();
    }

    @Override
    public Response<?> deleteCategory(Long id) {
        return null;
    }

    @Override
    public Response<List<CategoryDTO>> getAllCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return Response.<List<CategoryDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Categories fetched successfully")
                .data(categoryDTOs)
                .build();
    }
}
