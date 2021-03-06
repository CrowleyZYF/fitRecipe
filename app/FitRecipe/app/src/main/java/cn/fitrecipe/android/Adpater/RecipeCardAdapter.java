package cn.fitrecipe.android.Adpater;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.List;

import cn.fitrecipe.android.FrApplication;
import cn.fitrecipe.android.Http.FrRequest;
import cn.fitrecipe.android.Http.FrServerConfig;
import cn.fitrecipe.android.Http.PostRequest;
import cn.fitrecipe.android.R;
import cn.fitrecipe.android.RecipeActivity;
import cn.fitrecipe.android.entity.Recipe;
import cn.fitrecipe.android.function.Common;
import cn.fitrecipe.android.function.RequestErrorHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by 奕峰 on 2015/4/24.
 */
public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.RecipeCardViewHolder> implements View.OnClickListener {

    private List<Recipe> recipeCardsList;
    private List<Integer> recipeCardsListID;
    private Context context;
    private boolean isCollect;


    public RecipeCardAdapter(Context context, List<Recipe> recipeCardsList) {
        this.context = context;
        this.recipeCardsList = recipeCardsList;
        this.recipeCardsListID = null;
        this.isCollect = false;
    }

    public RecipeCardAdapter(Context context, List<Recipe> recipeCardsList, List<Integer> recipeCardsListID, boolean isCollect) {
        this.context = context;
        this.recipeCardsList = recipeCardsList;
        this.recipeCardsListID = recipeCardsListID;
        this.isCollect = isCollect;
    }

    @Override
    public RecipeCardAdapter.RecipeCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.framework_common_recipe_card, viewGroup, false);

        itemView.setOnClickListener(this);

        return new RecipeCardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecipeCardAdapter.RecipeCardViewHolder contactViewHolder, final int i) {
        Recipe rc = recipeCardsList.get(i);
        contactViewHolder.recipe_id.setText(rc.getId()+"");
        contactViewHolder.recipe_name.setText(rc.getTitle());
        contactViewHolder.recipe_function.setText(rc.getRecipe_function());
        contactViewHolder.recipe_function_backup.setText(rc.getRecipe_function_backup());
        if(rc.getRecipe_function().equals("完美")){
            contactViewHolder.recipe_function.setBackground(context.getResources().getDrawable(R.drawable.perfect_background));
        }else if(rc.getRecipe_function().equals("高蛋白")){
            contactViewHolder.recipe_function.setBackground(context.getResources().getDrawable(R.drawable.hp_background));
        }else if(rc.getRecipe_function().equals("低脂")){
            contactViewHolder.recipe_function.setBackground(context.getResources().getDrawable(R.drawable.lf_background));
        }else if(rc.getRecipe_function().equals("低卡")){
            contactViewHolder.recipe_function.setBackground(context.getResources().getDrawable(R.drawable.lk_background));
        }
        if(!rc.getRecipe_function_backup().equals("")){
            contactViewHolder.recipe_function_backup.setVisibility(View.VISIBLE);
            if(rc.getRecipe_function_backup().equals("完美")){
                contactViewHolder.recipe_function_backup.setBackground(context.getResources().getDrawable(R.drawable.perfect_background));
            }else if(rc.getRecipe_function_backup().equals("高蛋白")){
                contactViewHolder.recipe_function_backup.setBackground(context.getResources().getDrawable(R.drawable.hp_background));
            }else if(rc.getRecipe_function_backup().equals("低脂")){
                contactViewHolder.recipe_function_backup.setBackground(context.getResources().getDrawable(R.drawable.lf_background));
            }else if(rc.getRecipe_function_backup().equals("低卡")){
                contactViewHolder.recipe_function_backup.setBackground(context.getResources().getDrawable(R.drawable.lk_background));
            }
        }else{
            contactViewHolder.recipe_function_backup.setVisibility(View.GONE);
        }
        contactViewHolder.recipe_calorie.setText("热量： " + Math.round(rc.getCalories()) +"kcal/100g");
        contactViewHolder.recipe_time.setText("烹饪时间： " + rc.getDuration() + " min");
        contactViewHolder.recipe_like.setText("收藏 " + rc.getCollection_count());
        FrApplication.getInstance().getMyImageLoader().displayImage(contactViewHolder.recipe_background, rc.getThumbnail());
        if (isCollect){
            contactViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Common.infoDialog(context, "取消收藏", "确认取消" + recipeCardsList.get(i).getTitle() + "的收藏?")
                            .setCancelText("取消")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(final SweetAlertDialog sweetAlertDialog) {
                                    String url = FrServerConfig.getDeleteCollectionUrl("recipe", recipeCardsListID.get(i));
                                    PostRequest request = new PostRequest(url, FrApplication.getInstance().getToken(), new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject res) {
                                            recipeCardsList.remove(i);
                                            recipeCardsListID.remove(i);
                                            notifyDataSetChanged();
                                            sweetAlertDialog.dismiss();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError volleyError) {
                                            RequestErrorHelper.toast(context, volleyError);
                                        }
                                    });
                                    FrRequest.getInstance().request(request);
                                }
                            }).show();
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(recipeCardsList == null)
            return 0;
        else
            return recipeCardsList.size();
    }

    @Override
    public void onClick(View v) {
        String id = ((TextView) v.findViewById(R.id.recipe_id)).getText().toString();
        Intent intent=new Intent(context,RecipeActivity.class);
        intent.putExtra("id", id);
        //TODO
        context.startActivity(intent);
    }

    public static class RecipeCardViewHolder extends RecyclerView.ViewHolder {
        protected TextView recipe_name;
        protected TextView recipe_id;
        protected TextView recipe_function;
        protected TextView recipe_function_backup;
        protected TextView recipe_time;
        protected TextView recipe_calorie;
        protected TextView recipe_like;
        protected RelativeLayout recipe_background;
        public View itemView;

        public RecipeCardViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            recipe_name =  (TextView) itemView.findViewById(R.id.recipe_name);
            recipe_id = (TextView) itemView.findViewById(R.id.recipe_id);
            recipe_function = (TextView)  itemView.findViewById(R.id.recipe_function);
            recipe_function_backup = (TextView)  itemView.findViewById(R.id.recipe_function_backup);
            recipe_time = (TextView)  itemView.findViewById(R.id.recipe_time);
            recipe_calorie = (TextView) itemView.findViewById(R.id.recipe_calorie);
            recipe_like = (TextView) itemView.findViewById(R.id.recipe_like);
            recipe_background = (RelativeLayout) itemView.findViewById(R.id.recipe_background);
        }
    }
}
