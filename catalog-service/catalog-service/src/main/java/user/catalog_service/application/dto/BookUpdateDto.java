package user.catalog_service.application.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookUpdateDto {
    private Long id;

    private String title;

    private String author;

    private BigDecimal price;

    private Integer stock;

    private Integer year;

    private Long category_id;

    private String image_base64;
}
