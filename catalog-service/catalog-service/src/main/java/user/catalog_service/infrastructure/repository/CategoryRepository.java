package user.catalog_service.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import user.catalog_service.domain.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
