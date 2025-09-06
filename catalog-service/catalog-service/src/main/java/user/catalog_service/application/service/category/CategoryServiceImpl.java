package user.catalog_service.application.service.category;

import user.catalog_service.application.dto.CategoryDto;
import user.catalog_service.application.dto.CategoryResponse;
import user.catalog_service.application.dto.CategoryUpdate;
import user.catalog_service.domain.model.Category;
import user.catalog_service.infrastructure.exception.BadRequestException;
import user.catalog_service.infrastructure.exception.ResourceNotFoundException;
import user.catalog_service.infrastructure.repository.BookRepository;
import user.catalog_service.infrastructure.repository.CategoryRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;
    private final BookRepository bookRepository;

    public CategoryServiceImpl(CategoryRepository repo, BookRepository bookRepository) {
        this.repo = repo;
        this.bookRepository = bookRepository;
    }

    @Override
    public CategoryDto create(CategoryDto dto) {
        Category c = new Category();
        c.setName(dto.getName());
        Category saved = repo.save(c);

        dto.setId(saved.getId());
        return dto;
    }

    @Override
    public Page<CategoryResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(this::toResponse);
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category b = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id,
                List.of("CATEGORY NOT FOUND")));
        return toResponse(b);
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdate dto) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id,
                        List.of("CATEGORY NOT FOUND")));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            c.setName(dto.getName());
        }

        Category updated = repo.save(c);

        return CategoryResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse delete(Long id) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id,
                        List.of("CATEGORY NOT FOUND")));

        if (!bookRepository.findByCategoryId(id).isEmpty()) {
            throw new BadRequestException("Can't Delete this Category",
                    List.of("there are still books that use this category with id: " + id));
        }

        repo.delete(c);
        return new CategoryResponse(c.getId(), c.getName());
    }

    private CategoryResponse toResponse(Category c) {
        CategoryResponse r = new CategoryResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        return r;
    }
}
