package cn.fitrecipe.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import cn.fitrecipe.android.Http.FrRequest;
import cn.fitrecipe.android.Http.FrServerConfig;
import cn.fitrecipe.android.Http.GetRequest;
import cn.fitrecipe.android.entity.Article;
import cn.fitrecipe.android.function.RequestErrorHelper;

public class ArticleActivity extends Activity implements View.OnClickListener{

    private ImageView left_btn;
    private ImageView right_btn;
    private WebView webView;


    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge_article);

        article = (Article) getIntent().getSerializableExtra("article");

        //初始化
        initView();
        getData();
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("ArticleActivity");
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("ArticleActivity");
        MobclickAgent.onResume(this);
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    }

    private void getData() {
        String url = FrServerConfig.getArticleById(article.getId());
        GetRequest request = new GetRequest(url, FrApplication.getInstance().getToken(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                if(res.has("data")) {
                    JSONObject data = null;
                    try {
                        data = res.getJSONObject("data");
                        processData(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                RequestErrorHelper.toast(ArticleActivity.this, volleyError);
            }
        });
        FrRequest.getInstance().request(request);

    }

    private void processData(JSONObject data) {
        article = new Gson().fromJson(data.toString(), Article.class);
        webView.loadUrl(article.getWx_url());
    }

    private void initEvent() {

    }

    @Override
    public void onClick(View v) {

    }
}
