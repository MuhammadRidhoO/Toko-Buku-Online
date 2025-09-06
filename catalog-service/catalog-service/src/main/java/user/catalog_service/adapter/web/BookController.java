package user.catalog_service.adapter.web;

import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import user.catalog_service.application.dto.*;
import user.catalog_service.application.service.book.BookService;
import user.catalog_service.infrastructure.exception.ApiResponse;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Validated
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @PostMapping("/books")
    public ResponseEntity<?> create(@Valid @RequestBody BookDto dto) {
        var res = service.create(dto);

        return ResponseEntity.ok(ApiResponse.success("Success to Create Book", res));

    }

    @GetMapping("/books")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<BookResponse> p = service.list(q, categoryId, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", p.getContent());
        response.put("page", p.getNumber());
        response.put("size", p.getSize());
        response.put("totalElements", p.getTotalElements());
        response.put("totalPages", p.getTotalPages());

        return ResponseEntity.ok(ApiResponse.success("Success to get list book", response));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        var res = service.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Success to get book", res));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BookUpdateDto dto) {
        var res = service.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Success to update book", res));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> delete(@PathVariable Long id) {
        BookResponse res = service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Success to delete book", res));

    }

    @PostMapping("/books/{id}/decrement")
    public ResponseEntity<?> decrementStock(
            @PathVariable Long id,
            @RequestParam int qty) {
        BookResponse updated = service.decrementStock(id, qty);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock decremented",
                "data", updated));
    }

    @PostMapping("/books/{id}/increment")
    public ResponseEntity<?> incrementStock(
            @PathVariable Long id,
            @RequestParam int qty) {
        BookResponse updated = service.incrementStock(id, qty);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stock incremented",
                "data", updated));
    }
}