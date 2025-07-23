package com.phegon.FoodApp.menu.services;

import com.phegon.FoodApp.aws.AWSS3Service;
import com.phegon.FoodApp.category.entity.Category;
import com.phegon.FoodApp.category.repository.CategoryRepository;
import com.phegon.FoodApp.exceptions.BadRequestException;
import com.phegon.FoodApp.exceptions.NotFoundException;
import com.phegon.FoodApp.menu.dtos.MenuDTO;
import com.phegon.FoodApp.menu.entity.Menu;
import com.phegon.FoodApp.menu.repository.MenuRepository;
import com.phegon.FoodApp.response.Response;
import com.phegon.FoodApp.review.dtos.ReviewDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {


    private final MenuRepository menuRepository;
    private final CategoryRepository    categoryRepository;
    private final ModelMapper modelMapper;
    private final AWSS3Service awsS3Service;

    @Override
    public Response<MenuDTO> createMenu(MenuDTO menuDTO) {
        log.info("Creating new menu: {}", menuDTO.getName());
        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String imageUrl = null;

        MultipartFile imageFile = menuDTO.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        URL s3Url = awsS3Service.uploadFile("menus/" + imageName, imageFile);
        imageUrl = s3Url.toString();
        Menu menu = Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .price(menuDTO.getPrice())
                .imageUrl(imageUrl)
                .category(category)
                .build();
        Menu savedMenu = menuRepository.save(menu);

        return  Response.<MenuDTO>builder()
                .statusCode(200)
                .message("Menu created successfully")
                .data(modelMapper.map(savedMenu, MenuDTO.class))
                .build();
    }

    @Override
    public Response<MenuDTO> updateMenu(MenuDTO menuDTO) {
        log.info("Updating menu: {}", menuDTO.getName());
        Menu existingMenu = menuRepository.findById(menuDTO.getId())
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        Category category = categoryRepository.findById(menuDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        String imageUrl = existingMenu.getImageUrl();
        MultipartFile imageFile = menuDTO.getImageFile();

        // check if a new image is provided
        if (imageFile != null && !imageFile.isEmpty()) {
           // delete the old image from s3 if it exists
            if ( imageUrl != null && !imageUrl.isEmpty()) {
                String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                awsS3Service.deleteFile("menus/"+ keyName);

                log.info("Deleted old image from S3: {}", keyName);
            }
            // upload new image
            String imageName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl= awsS3Service.uploadFile("menus/" + imageName, imageFile);
            imageUrl = newImageUrl.toString();
        }

        if (menuDTO.getName() != null && !menuDTO.getName().isBlank()) existingMenu.setName(menuDTO.getName());
        if (menuDTO.getDescription() != null && !menuDTO.getDescription().isBlank()) {
            existingMenu.setDescription(menuDTO.getDescription());
        }
        if (menuDTO.getPrice() != null) existingMenu.setPrice(menuDTO.getPrice());

        existingMenu.setImageUrl(imageUrl);
        existingMenu.setCategory(category);

        Menu updatedMenu = menuRepository.save(existingMenu);

        return Response.<MenuDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu updated successfully")
                .data(modelMapper.map(updatedMenu, MenuDTO.class))
                .build();
    }

    @Override
    public Response<MenuDTO> getMenuById(Long id) {
        log.info("Fetching menu by ID: {}", id);

        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        MenuDTO menuDTO = modelMapper.map(existingMenu, MenuDTO.class);

        // Sort the reviews in descending order by ID
        if (menuDTO.getReviews() != null){
            menuDTO.getReviews().sort(Comparator.comparing(ReviewDTO::getId).reversed());
        }

        return null;
    }

    @Override
    public Response<?> deleteMenu(Long id) {
        log.info("Deleting menu with ID: {}", id);

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found"));

        // delete the image from s3 if it exists
        String imageUrl = menu.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String keyName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            awsS3Service.deleteFile("menus/" + keyName);
            log.info("Deleted image from S3: {}", keyName);
        }
        menuRepository.deleteById(id);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menu deleted successfully")
                .build();
    }

    @Override
    public Response<List<MenuDTO>> getMenus(Long categoryId, String search) {
        log.info("Fetching menus with categoryId: {} and search: {}", categoryId, search);

        Specification<Menu> spec = buildSpecification(categoryId, search);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");

        List<Menu> menuLists = menuRepository.findAll(spec, sort);

        List<MenuDTO> menuDTOs = menuLists.stream()
                .map(menu -> modelMapper.map(menu, MenuDTO.class))
                .toList();

        return Response.<List<MenuDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Menus fetched successfully")
                .data(menuDTOs)
                .build();
    }

    private Specification<Menu> buildSpecification(Long categoryId, String search) {
        return ((root, query, cb) -> {

            // list to accumulate all where conditions
            List<Predicate> predicates = new java.util.ArrayList<>();

            // add category condition if categoryId is provided
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0])); // convert list to array
        });
    }
}
