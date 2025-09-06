package user.catalog_service.application.service.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import user.catalog_service.application.dto.BookDto;
import user.catalog_service.application.dto.BookResponse;
import user.catalog_service.application.dto.BookUpdateDto;

public interface BookService {
    BookDto create(BookDto dto);

    Page<BookResponse> list(String q, Long categoryId, Pageable pageable);

    BookResponse getById(Long id);

    BookResponse update(Long id, BookUpdateDto dto);

    BookResponse delete(Long id);

    BookResponse decrementStock(Long id, int qty);

    BookResponse incrementStock(Long id, int qty);

}
