package cn.fitrecipe.android.model;

/**
 * Created by 奕峰 on 2015/4/24.
 */
public class RecipeCard {
    protected String recipe_name;
    protected int recipe_function;
    protected int recipe_time;
    protected int recipe_calorie;
    protected int recipe_like;
    protected static final String RECIPE_TIME_PREFIX = "烹饪时间：";
    protected static final String RECIPE_TIME_SUFFIX = "min";
    protected static final String RECIPE_CALORIE_PREFIX = "热量：";
    protected static final String RECIPE_CALORIE_SUFFIX = "kcal/人份";
    protected static final String RECIPE_LIKE_PREFIX = "收藏 ";

    public RecipeCard(){
        this.recipe_name="";
        this.recipe_function=0;
        this.recipe_time=0;
        this.recipe_calorie=0;
        this.recipe_like=0;
    }

    public RecipeCard(String name, int function, int time, int calorie, int like){
        this.recipe_name=name;
        this.recipe_function=function;
        this.recipe_time=time;
        this.recipe_calorie=calorie;
        this.recipe_like=like;
    }

    public String getRecipe_name(){
        return this.recipe_name;
    }

    public String getRecipe_function(){
        if(this.recipe_function==0){
            return "不限";
        }else if(this.recipe_function==1){
            return "增肌";
        }else{
            return "减脂";
        }
    }

    public String getRecipe_time(){
        return RECIPE_TIME_PREFIX+this.recipe_time+RECIPE_TIME_SUFFIX;
    }

    public String getRecipe_calorie(){
        return RECIPE_CALORIE_PREFIX+this.recipe_calorie+RECIPE_CALORIE_SUFFIX;
    }

    public String getRecipe_like(){
        return RECIPE_LIKE_PREFIX+this.recipe_like;
    }
}

