package cn.fitrecipe.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import cn.fitrecipe.android.Adpater.PlanElementAdapter;
import cn.fitrecipe.android.FrApplication;
import cn.fitrecipe.android.Http.FrRequest;
import cn.fitrecipe.android.Http.FrServerConfig;
import cn.fitrecipe.android.Http.GetRequest;
import cn.fitrecipe.android.Http.PostRequest;
import cn.fitrecipe.android.NutritionActivity;
import cn.fitrecipe.android.PlanChoiceActivity;
import cn.fitrecipe.android.PlayerActivity;
import cn.fitrecipe.android.R;
import cn.fitrecipe.android.ReportActivity;
import cn.fitrecipe.android.UI.LinearLayoutForListView;
import cn.fitrecipe.android.dao.FrDbHelper;
import cn.fitrecipe.android.entity.BasketRecord;
import cn.fitrecipe.android.entity.DatePlan;
import cn.fitrecipe.android.entity.DatePlanItem;
import cn.fitrecipe.android.entity.PlanComponent;
import cn.fitrecipe.android.entity.PunchRecord;
import cn.fitrecipe.android.entity.Report;
import cn.fitrecipe.android.entity.SeriesPlan;
import cn.fitrecipe.android.function.Common;
import cn.fitrecipe.android.function.JoinPlanHelper;
import cn.fitrecipe.android.function.JsonParseHelper;
import cn.fitrecipe.android.function.RequestErrorHelper;
import pl.tajchert.sample.DotsTextView;

/**
 * Created by 奕峰 on 2015/4/11.
 */
public class PlanFragment extends Fragment implements View.OnClickListener{

    //Report
    private Report report;

    private LinearLayoutForListView plans;
    private List<DatePlanItem> items;
    private DatePlan datePlan;
    private PlanElementAdapter adapter;
    private ImageView video_btn, next_btn, prev_btn, report_btn;
    private TextView plan_status_day, plan_status, diy_days, plan_name;
    private TextView other_plan_days;

    private LinearLayout loadingInterface;
    private DotsTextView dotsTextView;
    private LinearLayout info_container;

    public static final int BREAKFAST_CODE = 00;
    public static final int ADDMEAL_01_CODE = 01;
    public static final int LUNCH_CODE = 02;
    public static final int ADDMEAL_02_CODE = 03;
    public static final int SUPPER_CODE = 04;
    public static final int ADDMEAL_03_CODE = 05;

    private int pointer = 0;
    private Map<String, DatePlan> data;
    public static boolean isFresh = false;

    //保存切换计划的记录
    private Map<String, SeriesPlan> planMap;
    //保存计划打卡记录
    private Map<String, List<PunchRecord>> punchData;
    //保存菜篮子
    private Map<String, List<BasketRecord>> basketData;

    //使用第三方计划的时候，完成第（1/7）天
    private Map<String, String> indexDate;

    //network
    private boolean isError = false;
    private boolean isPreEnable = true;

    private ScrollView plan_scrollview;

    private long startTime;

    //punch count
    private TextView punch_count;
    private TextView change_plan_btn;

    public boolean getData = false;

    private String video_id = "";


    final String[] preferenceKeyText = {"", "breakfast_time", "add_meal_01_time", "lunch_time", "add_meal_02_time", "dinner_time", "add_meal_03_time"};
    final String[] preferenceKeySwitch = {"", "breakfast_check", "add_meal_01_check", "lunch_check", "add_meal_02_check", "dinner_check", "add_meal_03_check"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_plan, container, false);
        initView(v);
        initEvent();
        initData();
        return v;
    }

    private void initView(View v) {
        //信息页面
        info_container = (LinearLayout) v.findViewById(R.id.info_container);
        //计划区域
        plans = (LinearLayoutForListView) v.findViewById(R.id.plans);
        //打开视频
        video_btn = (ImageView) v.findViewById(R.id.video_btn);
        //增肌还是减脂
        plan_status = (TextView) v.findViewById(R.id.plan_status);
        //第几天
        plan_status_day = (TextView) v.findViewById(R.id.plan_status_day);
        //查看下一天的记录
        next_btn = (ImageView) v.findViewById(R.id.next_btn);
        //查看上一天的记录
        prev_btn = (ImageView) v.findViewById(R.id.prev_btn);
        //日期
        diy_days = (TextView) v.findViewById(R.id.diy_days);
        //计划名称
        plan_name = (TextView) v.findViewById(R.id.plan_name);
        //第三方计划的周期
        other_plan_days = (TextView) v.findViewById(R.id.other_plan_days);
        //加载页面
        loadingInterface = (LinearLayout) v.findViewById(R.id.loading_interface);
        dotsTextView = (DotsTextView) v.findViewById(R.id.dots);
        //互动
        plan_scrollview = (ScrollView) v.findViewById(R.id.plan_scrollview);
        //punch_count
        punch_count = (TextView) v.findViewById(R.id.punch_count);
        change_plan_btn = (TextView) v.findViewById(R.id.change_plan_btn);
        report_btn = (ImageView) v.findViewById(R.id.report_btn);
    }

    private void initEvent() {
        video_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        prev_btn.setOnClickListener(this);
        report_btn.setOnClickListener(this);
        change_plan_btn.setOnClickListener(this);
    }

    private void initData() {
        //获取报告
        report = FrDbHelper.getInstance(FrApplication.getInstance()).getReport();
        //初始化一日多餐
        /*SharedPreferences sp = FrApplication.getInstance().getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean[] isShow = new boolean[3];
        isShow[0] = sp.getBoolean("has_add_meal_01", true);
        isShow[1] = sp.getBoolean("has_add_meal_02", true);
        isShow[2] = sp.getBoolean("has_add_meal_03", true);
        adapter = new PlanElementAdapter(this, items, report, isShow);*/
        adapter = new PlanElementAdapter(this, items, report);
        plans.setAdapter(adapter);
        if(report == null) {
            hideLoading(true);
            return;
        }
        //显示健身状态
        if(report.isGoalType() == 0) {
            plan_status.setText("增肌第");
        }else{
            plan_status.setText("减脂第");
        }
        //获取前面两天，后面七天的记录
        String today = Common.getDate();
        getData(Common.getSomeDay(today, -2), Common.getSomeDay(today, 5), false);
    }

    // 获取服务器上的记录

    /**
     * 获取计划， 判断是从服务器获取，还是直接从本地获取
     * @param start 计划开始时间
     * @param end   计划结束时间
     * @param isPre true表示预取，获取成功之后不会切换计划switchplan, false表示刚进入计划获取的数据
     */
    private void getData(String start, String end, boolean isPre) {
        if(data == null)
            data = new HashMap<>();
        if(punchData == null)
            punchData = new TreeMap<>();
        if(indexDate == null)
            indexDate = new TreeMap<>();
        if(Common.isOpenNetwork(FrApplication.getInstance())) {
            start = Common.dateFormat(start);
            end = Common.dateFormat(end);
            getDataFromServer(start, end, isPre);
        }else
            getDataFromLocal(start, end);
    }


    /**
     * 从服务器获取切换计划的记录
     * @param start
     * @param end
     * @param isPre
     */
    private void getDataFromServer(final String start, final String end, final boolean isPre) {
        GetRequest request = new GetRequest(FrServerConfig.getDatePlanUrl(start, end), FrApplication.getInstance().getToken(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject res) {
                        if(res != null && res.has("data")) {
                            try {
                                JSONObject data = res.getJSONObject("data");
                                processData(data, start, end, isPre);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        hideLoading(true);
                        RequestErrorHelper.toast(FrApplication.getInstance(), volleyError);
                        getDataFromLocal(start, end);
                    }
                });
        FrRequest.getInstance().request(request);
    }

    /**
     * 处理从服务器器获取的数据，并根据获取数据中plan id去获取每个计划详情
     * @param data
     * @param start
     * @param end
     * @param isPre
     * @throws JSONException
     */
    private void processData(JSONObject data, String start, String end, boolean isPre) throws JSONException {
        punch_count.setText(data.getString("count"));
        Common.setPunchCount(FrApplication.getInstance(), Integer.parseInt(data.getString("count")));
        planMap = new ConcurrentHashMap<>();
        JSONArray punchs = data.getJSONArray("punch");
        for(int i = 0; i < punchs.length(); i++) {
            String date = punchs.getJSONObject(i).getString("date");
            PunchRecord pr = new PunchRecord();
            pr.setDate(date);
            pr.setImg(punchs.getJSONObject(i).getString("img"));
            pr.setId(punchs.getJSONObject(i).getInt("id"));
            switch (punchs.getJSONObject(i).getInt("type")) {
                case 0: pr.setType("breakfast");    break;
                case 1: pr.setType("add_meal_01");    break;
                case 2: pr.setType("lunch");    break;
                case 3: pr.setType("add_meal_02");    break;
                case 4: pr.setType("supper");    break;
                case 5: pr.setType("add_meal_03");    break;
            }
            if(punchData.containsKey(date)) {
                punchData.get(date).add(pr);
            }else {
                List<PunchRecord> prs = new ArrayList<>();
                prs.add(pr);
                punchData.put(date, prs);
            }
        }
        if(!data.getString("lastJoined").equals("null")) {
            planMap.put(data.getJSONObject("lastJoined").getString("joined_date"), JsonParseHelper.getSeriesPlanFromJson(data.getJSONObject("lastJoined").getJSONObject("plan")));//和日期对应
        }
        JSONArray calendar = data.getJSONArray("calendar");
        for(int i = 0; i < calendar.length(); i++) {
            planMap.put(calendar.getJSONObject(i).getString("joined_date"), JsonParseHelper.getSeriesPlanFromJson(calendar.getJSONObject(i).getJSONObject("plan")));//和日期对应
        }
        postprocess(start, end, isPre);
    }

    /**
     * 如果是老用户，直接推算出每天的计划，如果是新用户，需要为新用户创建新的默认自定义计划。如果是预取,不会创建
     * @param start
     * @param end
     * @param isPre
     * @throws JSONException
     */
    private void postprocess(String start, String end, final boolean isPre) throws JSONException {
        final Map<String, SeriesPlan> plans = new TreeMap<>();//所有的计划
        Set<String> keyset = planMap.keySet();
        Iterator<String> iterator = keyset.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            plans.put(key, planMap.get(key));
        }

        start = Common.dateFormatReverse(start);
        end = Common.dateFormatReverse(end);
        Set<String> keyset1 = plans.keySet();
        Iterator<String> iterator1 = keyset1.iterator();
        String nowDate = iterator1.hasNext() ? iterator1.next() : null;//第一个切换计划的时间点
        if(nowDate != null) {//如果等于null，说明是新用户
            if (Common.CompareDate(start, nowDate) < 0)//比较一下，如果用户查询的start比最早的nowDate还早，那就说明之前没有计划，因为已经传过来了lastJoined，所以用户最早的记录也应该从nowDate开始
                start = nowDate;
            processDatePlan(start, end, plans, nowDate, isPre);
            if(!isPre)
                hideLoading(false);
        }else {
            //新用户 默认设定自定义计划
            if(!isPre) {
                start = Common.getDate();
                nowDate = start;

                final String finalStart = start;
                final String finalEnd = end;
                final String finalNowDate = nowDate;
                new JoinPlanHelper(FrApplication.getInstance()).joinPersonalPlan(new JoinPlanHelper.CallBack() {
                    @Override
                    public void handle(Object... res) {
                        //Toast.makeText(FrApplication.getInstance(), "默认设置自定义计划", Toast.LENGTH_SHORT).show();
                        int id = (Integer) res[0];
                        plans.put(finalStart, Common.gerneratePersonalPlan(id, FrApplication.getInstance()));
                        processDatePlan(finalStart, finalEnd, plans, finalNowDate, isPre);
                        hideLoading(false);
                    }
                }, nowDate);
            }else {
                //Toast.makeText(FrApplication.getInstance(), "预取 " + start + "-" + end + "为空！", Toast.LENGTH_SHORT).show();
                isPreEnable = false;
            }
        }
    }

    /**
     * 推算出每天的计划，并显示，在切换每天计划是，判断之前的6天计划是否已获取，如未获取，预取
     * @param start
     * @param end
     * @param plans
     * @param nowDate
     * @param isPre
     */
    private void processDatePlan(String start, String end, Map<String, SeriesPlan> plans, String nowDate, boolean isPre) {

        /**
         * 获取菜篮子信息
         */
        basketData = FrDbHelper.getInstance(FrApplication.getInstance()).getBasketInfo(start, end);
        SeriesPlan now = plans.get(nowDate);//切换的不同计划
        String str = start;
        while(Common.CompareDate(str, end) <= 0) {

            if(plans.containsKey(str)) {
                nowDate = str;
                now = plans.get(str);
            }
            /**
             * nowDate是切换不同计划的时间点 str是自然时间 自定义计划是每天都会创建不同的
             * 所以
             * 1. 对于自定义计划来说 如果nowDate和str相同，就说明那天创建了自定义计划，直接放到记录里，否则就说明那天没有创建，为空
             * 2. 对于第三方计划来说 则取某一天的计划 自动轮循
             * */
            DatePlan datePlan = null;
            if(now.getTitle().equals("personal plan")) {
                if(nowDate.equals(str)) {
                    datePlan = plans.get(str).getDatePlans().get(0);
                    datePlan.setPlan_id(plans.get(str).getId());
                    datePlan.setDate(str);
                    data.put(str, datePlan);
                }
                else {
                    datePlan = Common.gernerateEmptyPlan(str, FrApplication.getInstance());
                    data.put(str, datePlan);
                }
            }else {
                int th = Common.getDiff(str, nowDate) % now.getTotal_days();
                datePlan = now.getDatePlans().get(th);
                datePlan.setDate(str);
                datePlan.setPlan_id(now.getId());
                data.put(str, now.getDatePlans().get(th));
                indexDate.put(str, "("+ (th+1) +"/"+now.getTotal_days()+")");
            }
            str = Common.getSomeDay(str, 1);
        }
        if(!isPre)
            switchPlan(pointer, 1);
    }



    //pointer是往前或者往后几天 dir是之前还是之后
    private boolean switchPlan(int pointer, int dir) {
        String str = Common.getSomeDay(Common.getDate(), pointer);//获取几月几号的记录
        int days = Common.getDiff(str, report.getUpdatetime()) + 1;//增肌减脂第几天
        if (days > 0 && data.containsKey(str)) {

            /**
             * 预取
             */
            if (isPreEnable)
                prefetch(str);

            diy_days.setText(str);
            plan_status_day.setText(days + "");
            datePlan = data.get(str);
            datePlan.setDate(str);
            plan_name.setText(datePlan.getPlan_name().equals("personal plan") ? "自定义计划" : datePlan.getPlan_name());
            other_plan_days.setVisibility(datePlan.getPlan_name().equals("personal plan") ? View.GONE : View.VISIBLE);
            other_plan_days.setText(indexDate != null && indexDate.containsKey(str) ? indexDate.get(str) : "");
            items = datePlan.getItems();
            video_id = datePlan.getVideo();
            if (datePlan.getPlan_name().equals("personal plan")){
                video_btn.setVisibility(View.GONE);
            } else {
                video_btn.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < items.size(); i++)
                items.get(i).setDate(datePlan.getDate());

            if (punchData.containsKey(str)) {
                List<PunchRecord> prs = punchData.get(str);
                for (int i = 0; i < prs.size(); i++) {
                    switch (prs.get(i).getType()) {
                        case "breakfast":
                            items.get(0).setIsPunch(true);
                            items.get(0).setPunchId(prs.get(i).getId());
                            items.get(0).setImageCover(prs.get(i).getImg());
                            break;
                        case "add_meal_01":
                            items.get(1).setIsPunch(true);
                            items.get(1).setPunchId(prs.get(i).getId());
                            items.get(1).setImageCover(prs.get(i).getImg());
                            break;
                        case "lunch":
                            items.get(2).setIsPunch(true);
                            items.get(2).setPunchId(prs.get(i).getId());
                            items.get(2).setImageCover(prs.get(i).getImg());
                            break;
                        case "add_meal_02":
                            items.get(3).setIsPunch(true);
                            items.get(3).setPunchId(prs.get(i).getId());
                            items.get(3).setImageCover(prs.get(i).getImg());
                            break;
                        case "supper":
                            items.get(4).setIsPunch(true);
                            items.get(4).setPunchId(prs.get(i).getId());
                            items.get(4).setImageCover(prs.get(i).getImg());
                            break;
                        case "add_meal_03":
                            items.get(5).setIsPunch(true);
                            items.get(5).setPunchId(prs.get(i).getId());
                            items.get(5).setImageCover(prs.get(i).getImg());
                            break;
                    }
                }
            }
            if (basketData.containsKey(str)) {
                List<BasketRecord> brs = basketData.get(str);
                for (int i = 0; i < brs.size(); i++) {
                    switch (brs.get(i).getType()) {
                        case "breakfast":
                            items.get(0).setIsInBasket(true);
                            break;
                        case "add_meal_01":
                            items.get(1).setIsInBasket(true);
                            break;
                        case "lunch":
                            items.get(2).setIsInBasket(true);
                            break;
                        case "add_meal_02":
                            items.get(3).setIsInBasket(true);
                            break;
                        case "supper":
                            items.get(4).setIsInBasket(true);
                            break;
                        case "add_meal_03":
                            items.get(5).setIsInBasket(true);
                            break;
                    }
                }
            }
            SharedPreferences sp = FrApplication.getInstance().getSharedPreferences("user", Context.MODE_PRIVATE);
            /*boolean[] isShow = {true, true, true};
            if(datePlan.getPlan_name().equals("personal plan")) {
                isShow = new boolean[3];
                isShow[0] = sp.getBoolean("has_add_meal_01", true);
                isShow[1] = sp.getBoolean("has_add_meal_02", true);
                isShow[2] = sp.getBoolean("has_add_meal_03", true);
            }*/



            for (int i = 0; i < items.size(); i++) {
                switch (items.get(i).getType()) {
                    case "breakfast":
                        items.get(i).setTime(sp.getString(preferenceKeyText[1], "07:30"));
                        break;
                    case "add_meal_01":
                        items.get(i).setTime(sp.getString(preferenceKeyText[2], "10:00"));
                        break;
                    case "lunch":
                        items.get(i).setTime(sp.getString(preferenceKeyText[3], "12:00"));
                        break;
                    case "add_meal_02":
                        items.get(i).setTime(sp.getString(preferenceKeyText[4], "15:00"));
                        break;
                    case "supper":
                        items.get(i).setTime(sp.getString(preferenceKeyText[5], "17:30"));
                        break;
                    case "add_meal_03":
                        items.get(i).setTime(sp.getString(preferenceKeyText[6], "22:00"));
                        break;
                }
            }

            if (pointer != 0) {
                //adapter.setData(items, datePlan.getPlan_name().equals("personal plan") ? true : false, false, isShow, Common.CompareDate(diy_days.getText().toString(), Common.getDate()));
                adapter.setData(items, datePlan.getPlan_name().equals("personal plan") ? true : false, false, Common.CompareDate(diy_days.getText().toString(), Common.getDate()));
            }else {
                //adapter.setData(items, datePlan.getPlan_name().equals("personal plan") ? true : false, true, isShow, Common.CompareDate(diy_days.getText().toString(), Common.getDate()));//今天
                adapter.setData(items, datePlan.getPlan_name().equals("personal plan") ? true : false, true, Common.CompareDate(diy_days.getText().toString(), Common.getDate()));//今天
            }
            return true;
        }
        else {
            //Toast.makeText(FrApplication.getInstance(), "已经无计划了!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 预取str之前5天的计划
     * @param str
     */
    private void prefetch(String str) {
        String end = str;
        for(int i = 0; i < 6; i++) {
            end = Common.getSomeDay(str, -i);
            if(!data.containsKey(end)) {
                break;
            }
        }
        String start = Common.getSomeDay(str, -5);
        if(Common.CompareDate(report.getUpdatetime(), start) >= 0) {
            start = report.getUpdatetime();
            isPreEnable = false;
        }
        if(Common.CompareDate(start, end) <= 0) {
            //Toast.makeText(FrApplication.getInstance(), "预取" + start + "-" + end, Toast.LENGTH_SHORT).show();
            getData(start, end, true);
        }
    }

    private void getDataFromLocal(String start, String end) {

    }


    private void hideLoading(boolean isError){
        getData = true;
        loadingInterface.setVisibility(View.GONE);
        dotsTextView.stop();
        if(isError){
            //Toast.makeText(FrApplication.getInstance(), errorMessage, Toast.LENGTH_LONG).show();
        }else{
            info_container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (FrApplication.getInstance().isJustChangePlan()){
            FrApplication.getInstance().setJustChangePlan(false);
            punchData.remove(Common.getDate());
            scrollToTop();
        }

        if(FrApplication.getInstance().getPlanInUse() != null) {
            String select_name = FrApplication.getInstance().getPlanInUse().getTitle();
            String plan_name = "";
            String today = Common.getDate();
            if(data.containsKey(today)) {
                plan_name = data.get(today).getPlan_name();
                if(!plan_name.equals(select_name)) {
                    changePlan(today, FrApplication.getInstance().getPlanInUse());
                }
            }
        }

        if(FrApplication.getInstance().isBasketEmpty() && basketData != null) {
            basketData.clear();
            for(int i = 0; i < items.size(); i++) {
                items.get(i).setIsInBasket(false);
            }
            adapter.notifyDataSetChanged();
            FrApplication.getInstance().setIsBasketEmpty(false);
        }

       if(FrApplication.getInstance().getComponent() != null) {
           String date = FrApplication.getInstance().getDate();
           int type = FrApplication.getInstance().getType();
           int plan_id = FrApplication.getInstance().getPlan_id();
           PlanComponent component = FrApplication.getInstance().getComponent();
           datePlan = data.get(date);
           datePlan.setPlan_id(plan_id);
           datePlan.setDate(date);
           List<DatePlanItem> items = datePlan.getItems();
           items.get(type).addContent(component);
           datePlan.setItems(items);
           data.put(date, datePlan);
           FrApplication.getInstance().setComponent(null);
           switchPlan(pointer, 0);
       }

        if(FrApplication.getInstance().getPr() != null) {
            PunchRecord pr = FrApplication.getInstance().getPr();
            punch(pr);
            FrApplication.getInstance().setPr(null);
            switchPlan(pointer, 0);
            punch_count.setText(Common.getPunchCount(FrApplication.getInstance())+"");
        }

        if(FrApplication.getInstance().isSettingChanged()) {
            switchPlan(pointer, 0);
            FrApplication.getInstance().setIsSettingChanged(false);
        }

        //scrollToTop();

        MobclickAgent.onPageStart("Plan");
    }

    public void scrollToTop(){
        plan_scrollview.smoothScrollTo(0,0);
    }

    private void changePlan(String today, SeriesPlan plan) {
        for(int i = 0; i < 6; i++) {
            String str = Common.getSomeDay(today, i);
            DatePlan datePlan = null;
            if(plan.getTitle().equals("personal plan")) {
                if(i == 0) {
                    datePlan = plan.getDatePlans().get(0);
                    datePlan.setPlan_id(plan.getId());
                }else {
                    datePlan = Common.gernerateEmptyPlan(str, FrApplication.getInstance());
                }
            }else {
                int th = i % plan.getTotal_days();
                datePlan = plan.getDatePlans().get(th);
                indexDate.put(str, "("+ (th+1) +"/"+ plan.getTotal_days()+")");
                datePlan.setPlan_id(plan.getId());
            }
            datePlan.setDate(str);
            data.put(str,datePlan);
        }
        switchPlan(pointer, 0);
    }


    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("Plan");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int index = 0;
        switch (requestCode) {
            case BREAKFAST_CODE:
                index = 0;
                break;
            case ADDMEAL_01_CODE:
                index = 1;
                break;
            case LUNCH_CODE:
                index = 2;
                break;
            case ADDMEAL_02_CODE:
                index = 3;
                break;
            case SUPPER_CODE:
                index = 4;
                break;
            case ADDMEAL_03_CODE:
                index = 5;
                break;
        }
        if(resultCode == getActivity().RESULT_OK && data.hasExtra("component_selected")) {
            PlanComponent obj = (PlanComponent) data.getSerializableExtra("component_selected");
            items.get(index).addContent(obj);
            try {
                update();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void toggle(String type) {
//        Toast.makeText(FrApplication.getInstance(), type.value()+"", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(FrApplication.getInstance(), NutritionActivity.class);
        boolean f = true;
       if(type.equals("all")) {
           f = false;
            for(int i = 0; i < items.size(); i++){
                if(items.get(i).getComponents() != null && items.get(i).getComponents().size()> 0) {
                    f = true;
                    break;
                }
            }
       }
        if(f) {
            if(datePlan != null)
                datePlan.setItems(items);
            intent.putExtra("itemtype", type);
            intent.putExtra("dateplan", datePlan);
            startActivity(intent);
        }else {
            Toast.makeText(FrApplication.getInstance(), "请添加食谱和食材！", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_btn:
                /*Intent intent = new Intent(FrApplication.getInstance(), IngredientActivity.class);
                startActivity(intent);*/
                /*if (plan_name.getText().toString().equals("自定义计划")){
                    Common.infoDialog(this.FrApplication.getInstance(), "没有视频", "自定义计划没有相关视频的~").show();
                }else */
                if (!FrApplication.getInstance().isCanYouKu()){
                    Common.infoDialog(getActivity(), "不兼容", "由于优酷官方提供的程序接口不支持64位手机，所以暂时不能播放视频，但是我们团队会尽快想办法修复哒~小伙伴们也可以去优酷或者腾讯搜索健食记观看视频哈").show();
                }else{
                    if(video_id.equals("")){
                        Common.infoDialog(getActivity(), "暂无视频", "视频还在录制中，敬请期待啦~").show();
                    }else{
                        Intent intent  = new Intent(FrApplication.getInstance(), PlayerActivity.class);
                        intent.putExtra("vid", video_id);
                        startActivity(intent);
                    }
                }
                break;
            case R.id.change_plan_btn:
                startActivity(new Intent(FrApplication.getInstance(), PlanChoiceActivity.class));
                break;
            case R.id.next_btn:
                if(switchPlan(pointer + 1, 1)) {
                    pointer++;
                }
                break;
            case R.id.prev_btn:
                if(switchPlan(pointer - 1, -1)) {
                    pointer--;
                }
                break;
            case R.id.report_btn:
                startActivity(new Intent(FrApplication.getInstance(), ReportActivity.class));
                break;
        }
    }


    public void update() throws JSONException {
        JSONObject params = new JSONObject();
        JSONArray dish = new JSONArray();
        for(int i = 0; i < items.size(); i++) {
            JSONObject obj = new JSONObject();
            switch (items.get(i).getType()) {
                case "breakfast":
                    obj.put("type", 0);
                    break;
                case "add_meal_01":
                    obj.put("type", 1);
                    break;
                case "lunch":
                    obj.put("type", 2);
                    break;
                case "add_meal_02":
                    obj.put("type", 3);
                    break;
                case "supper":
                    obj.put("type", 4);
                    break;
                case "add_meal_03":
                    obj.put("type", 5);
                    break;
            }
            JSONArray ingredient = new JSONArray();
            JSONArray recipe = new JSONArray();
            ArrayList<PlanComponent> components = items.get(i).getComponents();
            if(components != null) {
                for(int j = 0; j <components.size(); j++) {
                    JSONObject obj1 = new JSONObject();
                    obj1.put("id", components.get(j).getId());
                    obj1.put("amount", components.get(j).getAmount());
                    if(components.get(j).getType() == 1)
                        recipe.put(obj1);
                    else
                        ingredient.put(obj1);
                }
            }
            obj.put("ingredient", ingredient);
            obj.put("recipe", recipe);
            dish.put(obj);
        }
        params.put("dish", dish);
        if(datePlan.getPlan_id() != -1)
            params.put("id", datePlan.getPlan_id());
        else {
            info_container.setEnabled(false);
            params.put("joined_date", datePlan.getDate());
        }
        PostRequest request = new PostRequest(FrServerConfig.getUpdatePlanUrl(), FrApplication.getInstance().getToken(), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                try {
                    datePlan.setPlan_id(res.getJSONObject("data").getInt("id"));
                    info_container.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Toast.makeText(FrApplication.getInstance(), "更新自定义计划成功！", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                info_container.setEnabled(true);
                RequestErrorHelper.toast(FrApplication.getInstance(), volleyError);
            }
        });
        FrRequest.getInstance().request(request);
    }

    public void addToBasket(String date, String type) {
        BasketRecord br = new BasketRecord();
        br.setType(type);
        br.setDate(date);
        List<BasketRecord> brs;
        if(basketData.containsKey(date)) {
            brs = basketData.get(date);
        }else
            brs = new ArrayList<>();
        brs.add(br);
        basketData.put(date, brs);
        FrApplication.getInstance().setBasketData(basketData);
    }

    public void removeFromBasket(String date, String type) {
        BasketRecord br = new BasketRecord();
        br.setType(type);
        br.setDate(date);
        List<BasketRecord> brs;
        if(basketData.containsKey(date)) {
            brs = basketData.get(date);
        }else
            brs = new ArrayList<>();
        for(int i = 0; i < brs.size(); i++) {
            if(brs.get(i).getType().equals(type) && brs.get(i).getDate().equals(date)) {
                brs.remove(i);
                break;
            }
        }
        basketData.put(date, brs);
        FrApplication.getInstance().setBasketData(basketData);
    }

    public void deletePunch(String date, String type) {
        PunchRecord pr = new PunchRecord();
        pr.setType(type);
        pr.setDate(date);
        List<PunchRecord> prs;
        if(punchData.containsKey(date)) {
            prs = punchData.get(date);
        }else
            prs = new ArrayList<>();
        for(int i = 0; i < prs.size(); i++) {
            if(prs.get(i).getType().equals(type) && prs.get(i).getDate().equals(date)) {
                prs.remove(i);
                break;
            }
        }
        punchData.put(date, prs);
        FrApplication.getInstance().setPunchData(punchData);
    }

    public void punch(PunchRecord pr) {
        List<PunchRecord> prs;
        String date = pr.getDate();
        if(punchData.containsKey(date)) {
            prs = punchData.get(date);
        }else
            prs = new ArrayList<>();
        prs.add(pr);
        punchData.put(date, prs);
        FrApplication.getInstance().setPunchData(punchData);
    }

}
