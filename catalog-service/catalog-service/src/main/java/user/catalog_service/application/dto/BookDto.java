package user.catalog_service.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {
    private Long id;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "author is required")
    private String author;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @NotNull(message = "stock is required")
    @Min(1)
    private Integer stock;

    @NotNull(message = "year is required")
    private Integer year;

    @NotNull(message = "category_id is required")
    private Long category_id;

    @NotNull(message = "image_base64 is required")
    private String image_base64;
}