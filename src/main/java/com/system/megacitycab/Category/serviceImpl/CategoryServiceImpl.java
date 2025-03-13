package com.system.megacitycab.Category.serviceImpl;

import com.system.megacitycab.Category.model.Category;
import com.system.megacitycab.Category.repository.CategoryRepository;
import com.system.megacitycab.Category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(String categoryId, Category category) {
        return categoryRepository.findById(categoryId)
                .map(existCategory ->{
                    existCategory.setCategoryName(category.getCategoryName());
                    existCategory.setPricePerKm(category.getPricePerKm());
                    return categoryRepository.save(existCategory);
                })
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public void deleteCategory(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
