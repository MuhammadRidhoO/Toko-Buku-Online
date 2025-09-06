package user.catalog_service.infrastructure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import user.catalog_service.domain.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
  Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author,
      Pageable pageable);

  Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

  List<Book> findByCategoryId(Long categoryId);

}