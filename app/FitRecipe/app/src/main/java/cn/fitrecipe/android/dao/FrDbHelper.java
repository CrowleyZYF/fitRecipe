package cn.fitrecipe.android.dao;

import android.content.Context;

import java.util.List;

import cn.fitrecipe.android.entity.Author;
import cn.fitrecipe.android.entity.Component;
import cn.fitrecipe.android.entity.Ingredient;
import cn.fitrecipe.android.entity.Nutrition;
import cn.fitrecipe.android.entity.Recipe;
import cn.fitrecipe.android.entity.Report;

/**
 * Created by wk on 2015/8/15.
 */
public class FrDbHelper {

    private static FrDbHelper instance;
    private static Context context;

    private FrDbHelper(Context context) {
        this.context = context;
    }

    public static FrDbHelper getInstance(Context context) {
        synchronized (FrDbHelper.class) {
            if(instance == null)
                instance = new FrDbHelper(context);
        }
        return instance;
    }

    public void addRecipe(Recipe recipe) {
        RecipeDao recipeDao = new RecipeDao(context);
        IngredientDao ingredientDao = new IngredientDao(context);
        ComponentDao componentDao = new ComponentDao(context);
        NutritionDao nutritionDao = new NutritionDao(context);

        recipeDao.add(recipe);
        List<Component> component_set = recipe.getComponent_set();
        for(int i = 0; i < component_set.size(); i++) {
            Component component = component_set.get(i);
            component.setRecipe(recipe);

            Ingredient ingredient = component.getIngredient();
            ingredientDao.add(ingredient);

            componentDao.add(component);
        }

        List<Nutrition> nutrition_set = recipe.getNutrition_set();
        for(int i = 0; i < nutrition_set.size(); i++) {
            Nutrition nutrition = nutrition_set.get(i);
            nutrition.setRecipe(recipe);
            nutritionDao.add(nutrition);
        }
    }

    public List<Recipe> getAllRecipe() {
        RecipeDao recipeDao = new RecipeDao(context);
        List<Recipe> recipes = recipeDao.getAll();
        if(recipes != null) {
            for(int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                recipe.setNutrition_set(new NutritionDao(context).getNutritions(recipe.getId()));
                recipe.setComponent_set(new ComponentDao(context).getComponents(recipe.getId()));
            }
        }
        return  recipes;
    }

    public List<Ingredient> getAllIngredient() {
        IngredientDao ingredientDao = new IngredientDao(context);
        return ingredientDao.getAll();
    }

    public Report getReport(Author author) {
        ReportDao dao = new ReportDao(context);
        return dao.getReport(author);
    }

    public void addReport(Report report) {
        ReportDao dao = new ReportDao(context);
        dao.add(report);
    }

    public void saveAuthor(Author author) {
        new AuthorDao(context).save(author);
    }

    public void authorLogout(Author author) {
        new AuthorDao(context).logout(author);
    }

    public Author getLoginAuthor() {
        return new AuthorDao(context).getAuthor();
    }
}