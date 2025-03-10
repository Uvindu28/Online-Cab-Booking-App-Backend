package com.system.megacitycab.Category.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "category")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category{
    @Id
    private String categoryId;
    private String categoryName;
    private String pricePerKm;

}
