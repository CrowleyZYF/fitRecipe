package cn.fitrecipe.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.fitrecipe.android.Adpater.ProcedureCardAdapter;
import cn.fitrecipe.android.UI.RecyclerViewLayoutManager;
import cn.fitrecipe.android.entity.Procedure;
import cn.fitrecipe.android.function.Common;

public class RecipeProcedureActivity extends Activity implements View.OnClickListener{

    private RecyclerView procedureRecyclerView;
    private ProcedureCardAdapter procedureCardAdapter;
    private RecyclerViewLayoutManager procedureLayoutManager;
    List<Procedure> procedureCards;
    private TextView title_view;
    private ImageView left_btn;
    private ImageView right_btn;
    private String title;
    private LinearLayout tips_area;
    private TextView tips_content;
    private String tips;
    private String video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_procedure);
        title = getIntent().getStringExtra("recipe_title");
        procedureCards = (ArrayList<Procedure>) getIntent().getSerializableExtra("procedure_set");
        tips = getIntent().getStringExtra("recipe_tips");
        video = getIntent().getStringExtra("video");
        initView();
        initData();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("RecipeProcedureActivity");
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("RecipeProcedureActivity");
        MobclickAgent.onResume(this);
    }

    private void initEvent() {
        left_btn.setOnClickListener(this);
        right_btn.setOnClickListener(this);
    }

    private void initData() {
        procedureCardAdapter = new ProcedureCardAdapter(this, procedureCards);
        procedureRecyclerView.setAdapter(procedureCardAdapter);
        if(tips.isEmpty()){
            tips_area.setVisibility(View.GONE);
        }else{
            tips_content.setText(tips);
        }

    }


    private void initView() {
        title_view = (TextView) findViewById(R.id.recipe_title);
        title_view.setText(title);
        left_btn = (ImageView) findViewById(R.id.left_btn);
        right_btn = (ImageView) findViewById(R.id.right_btn);
        procedureRecyclerView = (RecyclerView) this.findViewById(R.id.procedure_recycler_view);
        procedureLayoutManager = new RecyclerViewLayoutManager(this);
        procedureLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        procedureRecyclerView.setLayoutManager(procedureLayoutManager);
        tips_area = (LinearLayout) findViewById(R.id.tips_area);
        tips_content = (TextView) findViewById(R.id.tips_content);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                this.finish();
                break;
            case R.id.right_btn:
                if (!FrApplication.getInstance().isCanYouKu()){
                    Common.infoDialog(this, "不兼容", "由于优酷官方提供的程序接口不支持64位手机，所以暂时不能播放视频，但是我们团队会尽快想办法修复哒~小伙伴们也可以去优酷或者腾讯搜索健食记观看视频哈").show();
                } else{
                    if (video.equals("")){
                        Common.infoDialog(this, "暂无视频", "视频还在录制中，敬请期待啦~").show();
                    }else{
                        playVideo();
                    }
                }
                //Common.toBeContinuedDialog(this);
                break;
            default:
        }
    }

    private void playVideo() {
        Intent intent  = new Intent(RecipeProcedureActivity.this, PlayerActivity.class);
        intent.putExtra("vid", video);
        startActivity(intent);
    }
}
