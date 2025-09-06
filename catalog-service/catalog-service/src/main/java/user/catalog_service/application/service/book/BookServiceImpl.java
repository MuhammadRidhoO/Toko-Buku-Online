package user.catalog_service.application.service.book;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.*;
import user.catalog_service.application.dto.*;
import user.catalog_service.domain.model.*;
import user.catalog_service.infrastructure.exception.ResourceNotFoundException;
import user.catalog_service.infrastructure.repository.*;
import user.catalog_service.infrastructure.utils.ImageUtil;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;
    private final CategoryRepository categoryRepo;

    public BookServiceImpl(BookRepository bookRepo, CategoryRepository categoryRepo) {
        this.bookRepo = bookRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    @Transactional
    public BookDto create(BookDto dto) {
        List<String> errorMessages = new ArrayList<>();
        Category cat = null;
        if (dto.getCategory_id() != null) {
            cat = categoryRepo.findById(dto.getCategory_id())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found", errorMessages));
        }

        String base64Image = ImageUtil.encodeStringToBase64(dto.getImage_base64());

        Book b = new Book();
        b.setTitle(dto.getTitle());
        b.setAuthor(dto.getAuthor());
        b.setPrice(dto.getPrice());
        b.setStock(dto.getStock());
        b.setYear(dto.getYear());
        b.setImageBase64(base64Image);
        if (cat != null) {
            b.setCategory(cat);
        }
        Book saved = bookRepo.save(b);
        return toDto(saved);
    }

    @Override
    public Page<BookResponse> list(String q, Long categoryId, Pageable pageable) {
        Page<Book> page;
        if (q != null && !q.isBlank()) {
            page = bookRepo.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q, q, pageable);
        } else if (categoryId != null) {
            page = bookRepo.findByCategoryId(categoryId, pageable);
        } else {
            page = bookRepo.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    public BookResponse getById(Long id) {
        Book b = bookRepo.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Book not found with id: " + id,
                                List.of("BOOK NOT FOUND")));
        return toResponse(b);
    }

    @Override
    @Transactional
    public BookResponse update(Long id, BookUpdateDto dto) {
        Book b = bookRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id,
                        List.of("BOOK NOT FOUND")));

        if (dto.getCategory_id() != null) {
            Category cat = categoryRepo.findById(dto.getCategory_id())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id,
                            List.of("CATEGORY NOT FOUND")));
            b.setCategory(cat);
        }
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            b.setTitle(dto.getTitle());
        }
        if (dto.getAuthor() != null && !dto.getAuthor().isBlank()) {
            b.setAuthor(dto.getAuthor());
        }
        if (dto.getPrice() != null) {
            b.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            b.setStock(dto.getStock());
        }
        if (dto.getYear() != null) {
            b.setYear(dto.getYear());
        }
        if (dto.getImage_base64() != null && !dto.getImage_base64().isBlank()) {
            b.setImageBase64(dto.getImage_base64());
        }

        Book saved = bookRepo.save(b);
        return toResponse(saved);
    }

    @Override
    public BookResponse delete(Long id) {
        Book book = bookRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id,
                        List.of("BOOK NOT FOUND")));
        bookRepo.deleteById(id);
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getPrice(), book.getStock(),
                book.getYear(), book.getCategory().getId(), book.getImageBase64());
    }

    public Book save(Book book) {
        return bookRepo.save(book);
    }

    private BookDto toDto(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .stock(book.getStock())
                .year(book.getYear())
                .category_id(book.getCategory() != null ? book.getCategory().getId() : null)
                .image_base64(book.getImageBase64())
                .build();
    }

    @Override
    @Transactional
    public BookResponse decrementStock(Long id, int qty) {
        Book book = findEntityById(id);
        if (book.getStock() < qty) {
            throw new RuntimeException("Not enough stock for book id " + id);
        }
        book.setStock(book.getStock() - qty);
        Book saved = bookRepo.save(book);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookResponse incrementStock(Long id, int qty) {
        Book book = findEntityById(id);
        book.setStock(book.getStock() + qty);
        Book saved = bookRepo.save(book);
        return toResponse(saved);
    }

    private Book findEntityById(Long id) {
        return bookRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + id));
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .stock(book.getStock())
                .year(book.getYear())
                .category_id(book.getCategory() != null ? book.getCategory().getId() : null)
                .image_base64(book.getImageBase64())
                .build();
    }

}
