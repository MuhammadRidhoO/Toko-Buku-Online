package user.catalog_service.application.service.category;

import user.catalog_service.application.dto.CategoryDto;
import user.catalog_service.application.dto.CategoryResponse;
import user.catalog_service.application.dto.CategoryUpdate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDto create(CategoryDto dto);

    CategoryResponse getById(Long id);

    Page<CategoryResponse> list(Pageable pageable);

    CategoryResponse update(Long id, CategoryUpdate dto);

    CategoryResponse delete(Long id);
}