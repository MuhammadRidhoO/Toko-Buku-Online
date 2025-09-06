package user.catalog_service.adapter.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import user.catalog_service.application.dto.CategoryDto;
import user.catalog_service.application.dto.CategoryResponse;
import user.catalog_service.application.dto.CategoryUpdate;
import user.catalog_service.application.service.category.CategoryService;
import user.catalog_service.infrastructure.exception.ApiResponse;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryDto>> create(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = categoryService.create(dto);
        return ResponseEntity.ok(ApiResponse.success("Successful to Created", created));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<CategoryResponse> p = categoryService.list(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", p.getContent());
        response.put("page", p.getNumber());
        response.put("size", p.getSize());
        response.put("totalElements", p.getTotalElements());
        response.put("totalPages", p.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Find All Category", response));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        var res = categoryService.getById(id);

        return ResponseEntity.ok(ApiResponse.success("Find Category", res));

    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
            @RequestBody CategoryUpdate dto) {
        var res = categoryService.update(id, dto);
        // return ResponseEntity.ok(res);
        return ResponseEntity.ok(ApiResponse.success("Success to Update Category", res));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> delete(@PathVariable Long id) {
        CategoryResponse deleted = categoryService.delete(id);

        return ResponseEntity.ok(ApiResponse.success("Success to Delete Category", deleted));

    }
}
