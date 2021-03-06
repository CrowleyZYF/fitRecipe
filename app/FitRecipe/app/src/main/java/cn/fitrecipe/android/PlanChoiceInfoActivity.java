package cn.fitrecipe.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.fitrecipe.android.Adpater.PlanDetailViewPagerAdapter;
import cn.fitrecipe.android.Adpater.PlanInfoViewPagerAdapter;
import cn.fitrecipe.android.Http.FrRequest;
import cn.fitrecipe.android.Http.FrServerConfig;
import cn.fitrecipe.android.Http.GetRequest;
import cn.fitrecipe.android.UI.PlanDetailViewPager;
import cn.fitrecipe.android.UI.PlanScrollView;
import cn.fitrecipe.android.entity.DatePlan;
import cn.fitrecipe.android.entity.DatePlanItem;
import cn.fitrecipe.android.entity.PlanAuthor;
import cn.fitrecipe.android.entity.PlanComponent;
import cn.fitrecipe.android.entity.SeriesPlan;
import cn.fitrecipe.android.function.Common;
import cn.fitrecipe.android.function.JsonParseHelper;
import cn.fitrecipe.android.function.RequestErrorHelper;
import me.relex.circleindicator.CircleIndicator;
import pl.tajchert.sample.DotsTextView;

/**
 * Created by 奕峰 on 2015/5/8.
 */
public class PlanChoiceInfoActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private PlanScrollView info_container;

    private ViewPager planInfoViewPager;
    private PlanInfoViewPagerAdapter planInfoViewPagerAdapter;
    private CircleIndicator planInfoIndicator;

    private LinearLayout header;
    private TextView header_name, plan_day, plan_calories;
    private ImageView back_btn;
    private ImageView author_btn;
    private ImageView nutrition_btn;

    private LinearLayout loadingInterface;
    private DotsTextView dotsTextView;


    private LinearLayout choice_info_background;
    private PlanDetailViewPager planDetailViewPager;
    private PlanDetailViewPagerAdapter planDetailViewPagerAdapter;
    private ImageView prev_day_btn;
    private ImageView next_day_btn;
    private SeriesPlan plan;

    private int nowY;
    private boolean isUsed;
    private String video_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_choice_info);

        int plan_id =  getIntent().getIntExtra("plan_id", 0);
        isUsed = getIntent().getBooleanExtra("isUsed", false);
        initView();
        getData(plan_id);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("PlanChoiceInfoActivity");
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("PlanChoiceInfoActivity");
        MobclickAgent.onResume(this);
    }

    private void getData(int id) {
        String url = FrServerConfig.getPlanDetails(id);
        GetRequest request = new GetRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                if (res != null && res.has("data")) {
                    try {
                        JSONObject data = res.getJSONObject("data");
                        processData(data);
                        hideLoading(false, "");
                        initData();
                        initEvent();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                RequestErrorHelper.toast(PlanChoiceInfoActivity.this, volleyError);
            }
        });
        FrRequest.getInstance().request(request);

    }

    private void processData(JSONObject data) throws JSONException{
        plan = new SeriesPlan();
        plan.setId(data.getInt("id"));
        plan.setImg(data.getString("img"));
        plan.setInrtoduce(data.getString("inrtoduce"));
        plan.setDifficulty(data.getInt("difficulty"));
        plan.setDelicious(data.getInt("delicious"));
        plan.setBenifit(data.getInt("benifit"));
        plan.setTotal_days(data.getInt("total_days"));
        plan.setDish_headcount(data.getInt("dish_headcount"));
        plan.setBrief(data.getString("brief"));
        plan.setTitle(data.getString("title"));
        plan.setCover(data.getString("cover"));

        JSONObject author_json = data.getJSONObject("author");
        plan.setAuthor(new Gson().fromJson(author_json.toString(), PlanAuthor.class));

        ArrayList<DatePlan> datePlans = new ArrayList<>();

        JSONArray routine_set = data.getJSONArray("routine_set");
        for(int i  = 0; i < routine_set.length(); i++) {
            JSONObject routine = routine_set.getJSONObject(i);
            DatePlan datePlan = new DatePlan();
            datePlan.setVideo(routine.getString("video"));
            datePlan.setPlan_name(plan.getTitle());
            JSONArray dish_set = routine.getJSONArray("dish_set");
            List<DatePlanItem> items = new ArrayList<>();
            for(int j = 0; j < dish_set.length(); j++) {
                DatePlanItem item = new DatePlanItem();
                int type = dish_set.getJSONObject(j).getInt("type");
                switch (type) {
                    case 0:
                        item.setType("breakfast");
                        item.setDefaultImageCover("drawable://" + R.drawable.breakfast);
                        break;
                    case 1:
                        item.setType("add_meal_01");
                        item.setDefaultImageCover("drawable://" + R.drawable.add_meal_01);
                        break;
                    case 2:
                        item.setType("lunch");
                        item.setDefaultImageCover("drawable://" + R.drawable.lunch);
                        break;
                    case 3:
                        item.setType("add_meal_02");
                        item.setDefaultImageCover("drawable://" + R.drawable.add_meal_02);
                        break;
                    case 4:
                        item.setType("supper");
                        item.setDefaultImageCover("drawable://" + R.drawable.dinner);
                        break;
                    case 5:
                        item.setType("add_meal_03");
                        item.setDefaultImageCover("drawable://" + R.drawable.add_meal_03);
                        break;
                }
                JSONArray singleingredient_set = dish_set.getJSONObject(j).getJSONArray("singleingredient_set");
                for(int k = 0; k < singleingredient_set.length(); k++) {
                    PlanComponent component = new PlanComponent();
                    component.setType(0);
                    component.setName(singleingredient_set.getJSONObject(k).getJSONObject("ingredient").getString("name"));
                    component.setNutritions(JsonParseHelper.getNutritionSetFromJson(singleingredient_set.getJSONObject(k).getJSONObject("ingredient").getJSONObject("nutrition_set")));
                    component.setAmount(singleingredient_set.getJSONObject(k).getInt("amount"));
                    component.setCalories(singleingredient_set.getJSONObject(k).getJSONObject("ingredient").getJSONObject("nutrition_set").getJSONObject("Energy").getDouble("amount"));
//                    componentList.add(component);
                    item.addContent(component);
                }
                JSONArray singlerecipe_set = dish_set.getJSONObject(j).getJSONArray("singlerecipe_set");
                for(int k = 0; k < singlerecipe_set.length(); k++) {
                    JSONObject json_recipe = singlerecipe_set.getJSONObject(k);
                    PlanComponent component = new PlanComponent();
                    component.setAmount(json_recipe.getInt("amount"));
                    component.setCalories(json_recipe.getJSONObject("recipe").getDouble("calories"));
                    component.setType(1);
                    component.setId(json_recipe.getJSONObject("recipe").getInt("id"));
                    component.setName(json_recipe.getJSONObject("recipe").getString("title"));
                    component.setNutritions(JsonParseHelper.getNutritionSetFromJson(json_recipe.getJSONObject("recipe").getJSONObject("nutrition_set")));
                    JSONArray json_component = json_recipe.getJSONObject("recipe").getJSONArray("component_set");
                    ArrayList<PlanComponent> components = new ArrayList<>();
                    for(int q = 0; q < json_component.length(); q++) {
                        PlanComponent component1 = new PlanComponent();
                        JSONObject jcomponent = json_component.getJSONObject(q);
                        component1.setName(jcomponent.getJSONObject("ingredient").getString("name"));
                        component1.setAmount(jcomponent.getInt("amount"));
                        component1.setType(0);
                        components.add(component1);
                    }
                    component.setComponents(components);
//                    componentList.add(component);
                    item.addContent(component);
                }
//                item.setComponents(componentList);
                items.add(item);
            }
            datePlan.setItems(items);
            datePlans.add(datePlan);
        }
        plan.setDatePlans(datePlans);
        plan.setIsUsed(isUsed);
    }

    private void hideLoading(boolean isError, String errorMessage){
        loadingInterface.setVisibility(View.GONE);
        dotsTextView.stop();
        if(isError){
            //Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }else{
            info_container.setVisibility(View.VISIBLE);
            info_container.smoothScrollTo(0, 0);
        }
    }

    private void initView() {
        info_container = (PlanScrollView) findViewById(R.id.info_container);

        planInfoViewPager = (ViewPager) findViewById(R.id.choice_intro_viewpager);
        planInfoIndicator = (CircleIndicator) findViewById(R.id.choice_intro_indicator);

        header = (LinearLayout) findViewById(R.id.header);
        header.setBackgroundColor(getResources().getColor(R.color.transparent));
        header_name = (TextView) findViewById(R.id.header_name_text);
        plan_day = (TextView) findViewById(R.id.plan_day);
        plan_calories = (TextView) findViewById(R.id.plan_calories);
        header_name.setText("计划详情");
        back_btn = (ImageView) findViewById(R.id.left_btn);
        back_btn.setBackgroundColor(getResources().getColor(R.color.transparent));
        back_btn.setImageResource(R.drawable.icon_back_white);
        author_btn = (ImageView) findViewById(R.id.right_btn);
        author_btn.setBackgroundColor(getResources().getColor(R.color.transparent));
        author_btn.setImageResource(R.drawable.icon_video);
        //author_btn.setVisibility(View.GONE);
        planDetailViewPager = (PlanDetailViewPager) findViewById(R.id.plan_detail);
        prev_day_btn = (ImageView) findViewById(R.id.prev_day_btn);
        next_day_btn = (ImageView) findViewById(R.id.next_day_btn);
        nutrition_btn = (ImageView) findViewById(R.id.nutrition_btn);
        loadingInterface = (LinearLayout)findViewById(R.id.loading_interface);
        dotsTextView = (DotsTextView) findViewById(R.id.dots);

        choice_info_background = (LinearLayout) findViewById(R.id.choice_info_background);
    }

    private void initData() {
        plan_day.setText(1 + "/" + plan.getTotal_days());
        plan_calories.setText(Math.round(plan.getDatePlans().get(0).getTotalCalories())+"");
        video_id = plan.getDatePlans().get(0).getVideo();
        planDetailViewPagerAdapter = new PlanDetailViewPagerAdapter(this, plan);
        planDetailViewPager.setAdapter(planDetailViewPagerAdapter);
        planInfoViewPagerAdapter = new PlanInfoViewPagerAdapter(this, plan);
        planInfoViewPager.setAdapter(planInfoViewPagerAdapter);
        planInfoIndicator.setViewPager(planInfoViewPager);
        FrApplication.getInstance().getMyImageLoader().displayImage(choice_info_background, plan.getCover());
    }

    private void initEvent() {
        planInfoIndicator.setOnPageChangeListener(this);

        info_container.setOnBorderListener(new PlanScrollView.OnBorderListener(){

            @Override
            public void onBottom() {

            }

            @Override
            public void onTop() {
                header.setBackgroundColor(getResources().getColor(R.color.transparent));
            }

            @Override
            public void onScroll() {
                int scrollY=info_container.getScrollY();
                int transparent = 255/80*scrollY;
                if(transparent>255) {
                    transparent=255;
                }
                header.setBackgroundColor(Color.argb(transparent, 73, 189, 204));
            }
        });
        back_btn.setOnClickListener(this);
        author_btn.setOnClickListener(this);
        nutrition_btn.setOnClickListener(this);
        prev_day_btn.setOnClickListener(this);
        next_day_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.left_btn:{
                Intent intent = new Intent();
                intent.putExtra("plan_id", plan.getId());
                intent.putExtra("isUsed", plan.isUsed());
                setResult(RESULT_OK, intent);
                finish();
                break;
            }
            case R.id.nutrition_btn:{
                Intent intent = new Intent(this, PlanNutritionActivity.class);
                intent.putExtra("plan", plan);
                intent.putExtra("plan_day", plan_day.getText().toString());
                startActivity(intent);
                break;
            }
            case R.id.prev_day_btn:{
                goPrev();
                break;
            }
            case R.id.next_day_btn:{
                goNext();
                break;
            }
            case R.id.right_btn:{
                if (!FrApplication.getInstance().isCanYouKu()){
                    Common.infoDialog(this, "不兼容", "由于优酷官方提供的程序接口不支持64位手机，所以暂时不能播放视频，但是我们团队会尽快想办法修复哒~小伙伴们也可以去优酷或者腾讯搜索健食记观看视频哈").show();
                } else {
                    if (loadingInterface.getVisibility()==View.VISIBLE){
                        Common.infoDialog(this, "稍等一下", "数据正在读取中~").show();
                    }else if (video_id.equals("")){
                        Common.infoDialog(this, "暂无视频", "视频还在录制中，敬请期待啦~").show();
                    }else{
                        Intent intent  = new Intent(this, PlayerActivity.class);
                        intent.putExtra("vid", video_id);
                        startActivity(intent);
                    }
                }
                break;
            }
        }
    }

    public void goNext(){
        nowY = info_container.getScrollY();
        planDetailViewPager.setCurrentItem(planDetailViewPager.getCurrentItem()+1, true);
        plan_day.setText((planDetailViewPager.getCurrentItem()+1) + "/" + plan.getTotal_days());
        plan_calories.setText(Math.round(plan.getDatePlans().get(planDetailViewPager.getCurrentItem()).getTotalCalories())+"");
        video_id = plan.getDatePlans().get(planDetailViewPager.getCurrentItem()).getVideo();
        info_container.smoothScrollTo(0, nowY);
    }

    public void goPrev(){
        nowY = info_container.getScrollY();
        planDetailViewPager.setCurrentItem(planDetailViewPager.getCurrentItem()-1, true);
        plan_day.setText((planDetailViewPager.getCurrentItem()+1) + "/" + plan.getTotal_days());
        plan_calories.setText(Math.round(plan.getDatePlans().get(planDetailViewPager.getCurrentItem()).getTotalCalories()) + "");
        video_id = plan.getDatePlans().get(planDetailViewPager.getCurrentItem()).getVideo();
        info_container.smoothScrollTo(0, nowY);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
