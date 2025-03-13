package com.system.megacitycab.Category.service;

import com.system.megacitycab.Category.model.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<Category> getAllCategories();
    Category createCategory(Category category);
    Category updateCategory(String categoryId, Category category);
    void deleteCategory(String categoryId);
}
