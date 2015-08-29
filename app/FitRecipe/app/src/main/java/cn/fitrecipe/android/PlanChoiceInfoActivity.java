package cn.fitrecipe.android;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.fitrecipe.android.Adpater.PlanDetailViewPagerAdapter;
import cn.fitrecipe.android.UI.PlanDetailViewPager;
import cn.fitrecipe.android.UI.PlanScrollView;
import cn.fitrecipe.android.entity.PlanDetail;
import cn.fitrecipe.android.entity.PlanDetailItem;

/**
 * Created by 奕峰 on 2015/5/8.
 */
public class PlanChoiceInfoActivity extends Activity implements View.OnTouchListener {

    private PlanScrollView info_container;

    private LinearLayout header;
    private ImageView back_btn;
    private ImageView author_btn;

    private PlanDetailViewPager planDetailViewPager;
    private PlanDetailViewPagerAdapter planDetailViewPagerAdapter;
    private List<PlanDetail> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_choice_info);

        initView();
        initData();
        initEvent();
    }

    private void initView() {
        info_container = (PlanScrollView) findViewById(R.id.info_container);
        header = (LinearLayout) findViewById(R.id.header);
        header.setBackgroundColor(getResources().getColor(R.color.transparent));
        back_btn = (ImageView) findViewById(R.id.left_btn);
        back_btn.setBackgroundColor(getResources().getColor(R.color.transparent));
        back_btn.setImageResource(R.drawable.icon_back_white);
        author_btn = (ImageView) findViewById(R.id.right_btn);
        author_btn.setBackgroundColor(getResources().getColor(R.color.transparent));
        author_btn.setImageResource(R.drawable.icon_user);
        planDetailViewPager = (PlanDetailViewPager) findViewById(R.id.plan_detail);
    }

    private void initData() {
        PlanDetailItem item01 = new PlanDetailItem(1,1,"无糖豆浆",200,100);
        PlanDetailItem item02 = new PlanDetailItem(2,1,"鸡蛋",50,100);
        PlanDetailItem item03 = new PlanDetailItem(3,1,"燕麦片",40,150);
        PlanDetailItem item04 = new PlanDetailItem(4,1,"青菜",100,50);
        List<PlanDetailItem> list1 = new ArrayList<>();
        list1.add(item01);
        list1.add(item02);
        list1.add(item03);
        list1.add(item04);

        PlanDetailItem item05 = new PlanDetailItem(5,1,"鹰嘴豆",100,200);
        List<PlanDetailItem> list2 = new ArrayList<>();
        list2.add(item05);

        PlanDetailItem item06 = new PlanDetailItem(6,1,"牛肉",250,300);
        PlanDetailItem item07 = new PlanDetailItem(7,1,"杂粮饭",100,100);
        PlanDetailItem item08 = new PlanDetailItem(8,1,"蔬菜色拉",100,150);
        List<PlanDetailItem> list3 = new ArrayList<>();
        list3.add(item06);
        list3.add(item07);
        list3.add(item08);

        PlanDetailItem item09 = new PlanDetailItem(9,1,"牛奶",200,100);
        PlanDetailItem item10 = new PlanDetailItem(10,1,"杂粮粥",200,100);
        PlanDetailItem item11 = new PlanDetailItem(10,1,"玉米片",200,100);
        List<PlanDetailItem> list4 = new ArrayList<>();
        list4.add(item09);
        list4.add(item10);
        list4.add(item11);

        PlanDetailItem item12 = new PlanDetailItem(10,1,"猕猴桃",200,100);
        List<PlanDetailItem> list5 = new ArrayList<>();
        list5.add(item12);

        PlanDetailItem item13 = new PlanDetailItem(10,1,"牛肉",200,100);
        PlanDetailItem item14 = new PlanDetailItem(10,1,"杂豆粗粮饭",200,100);
        PlanDetailItem item15 = new PlanDetailItem(10,1,"水果色拉",200,100);
        List<PlanDetailItem> list6 = new ArrayList<>();
        list6.add(item13);
        list6.add(item14);
        list6.add(item15);

        dataList = new ArrayList<>();
        PlanDetail day1 = new PlanDetail(1,1,1200,300,1200,list1,true,100,1300,list2,true,400,3200,list3,true,600,5600,list1,true,200,3200,list2,true);
        PlanDetail day2 = new PlanDetail(2,2,1300,200,1100,list4,true,200,1100,list5,true,500,2200,list6,true,320,1600,list1,true,200,3200,list2,true);
        dataList.add(day1);
        dataList.add(day2);

        planDetailViewPagerAdapter = new PlanDetailViewPagerAdapter(this,dataList);
        planDetailViewPager.setAdapter(planDetailViewPagerAdapter);
    }

    private void initEvent() {

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
                if(transparent>255){
                    transparent=255;
                }
                header.setBackgroundColor(Color.argb(transparent, 73, 189, 204));
            }
        });
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int scrollY=v.getScrollY();
        int mh = v.getMeasuredHeight();
        int h = v.getHeight();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                if(scrollY==0){

                }else{
                    int transparent = 256/40*scrollY;
                    if(transparent>256){
                        transparent=255;
                    }
                    header.setBackgroundColor(Color.argb(transparent, 73, 189, 204));
                }
                break;
            case MotionEvent.ACTION_SCROLL:
                System.out.println("y: "+ scrollY+ "  mh:" + mh + " h: " + h);
                break;
            default:
                break;
        }
        return false;
    }
}
