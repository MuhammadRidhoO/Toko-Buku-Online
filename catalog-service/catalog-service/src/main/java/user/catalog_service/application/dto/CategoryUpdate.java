package user.catalog_service.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdate {
    private Long id;

    private String name;
}
