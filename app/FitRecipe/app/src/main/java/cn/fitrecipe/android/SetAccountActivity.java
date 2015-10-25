package cn.fitrecipe.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.util.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cn.fitrecipe.android.Http.FrRequest;
import cn.fitrecipe.android.Http.FrServerConfig;
import cn.fitrecipe.android.Http.PostRequest;
import cn.fitrecipe.android.entity.Author;
import cn.fitrecipe.android.fragment.MeFragment;
import cn.fitrecipe.android.function.Common;
import cn.fitrecipe.android.function.RequestErrorHelper;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 奕峰 on 2015/5/8.
 */
public class SetAccountActivity extends Activity implements View.OnClickListener {

    private RelativeLayout set_account_avatar;
    private EditText set_account_nickname;
    private TextView set_account_gender, set_account_age, set_account_height, set_account_weight, set_account_fat, set_account_target;
    private TextView save_btn;
    private ImageView back_btn;
    private String pngName;
    private ProgressDialog pd;
    private CircleImageView me_avatar;
    private Bitmap bitmap;

    private String nick_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_account);

        initView();
        initEvent();
    }

    private void initEvent() {
        back_btn.setOnClickListener(this);
        save_btn.setOnClickListener(this);
        set_account_avatar.setOnClickListener(this);
    }

    private void initView() {
        back_btn = (ImageView) findViewById(R.id.left_btn);
        save_btn = (TextView) findViewById(R.id.right_btn);
        set_account_avatar = (RelativeLayout) findViewById(R.id.set_account_avatar);
        set_account_nickname = (EditText) findViewById(R.id.set_account_nickname);
        set_account_gender = (TextView) findViewById(R.id.set_account_gender);
        set_account_age = (TextView) findViewById(R.id.set_account_age);
        set_account_height = (TextView) findViewById(R.id.set_account_height);
        set_account_weight = (TextView) findViewById(R.id.set_account_weight);
        set_account_fat = (TextView) findViewById(R.id.set_account_fat);
        set_account_target = (TextView) findViewById(R.id.set_account_target);
        me_avatar = (CircleImageView) findViewById(R.id.me_avatar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                finish();
                break;
            case R.id.right_btn:
                update();
                break;
            case R.id.set_account_avatar:
                Intent intent = new Intent(SetAccountActivity.this, ChoosePhotoActivity.class);
                intent.putExtra("from", "avatar");
                startActivityForResult(intent, 111);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra("bitmap");
            bitmap = BitmapFactory.decodeFile(path);
            me_avatar.setImageBitmap(bitmap);
        }
    }

    private void update() {

        checkValid();

        //上传
        pd = ProgressDialog.show(this, "", "正在发布...", true);
        pd.setCanceledOnTouchOutside(false);

        if (bitmap != null) {
            UploadManager uploadManager = new UploadManager();
            Auth auth = Auth.create("LV1xTmPqkwCWd3yW4UNn90aaXyPZCGPG-MdaA3Ob", "mfMEtgpxmdRrgM7No-AwtaFCkCM6mOVr_FYbq6MR");        //get token from access key and secret key
            String token = auth.uploadToken("fitrecipe");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            pngName = FrApplication.getInstance().getAuthor().getNick_name() + Common.getTime();
            //Toast.makeText(PunchContentSureActivity.this, pngName, Toast.LENGTH_SHORT).show();
            uploadManager.put(baos.toByteArray(), pngName, token, new UpCompletionHandler() {
                @Override
                public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                    Toast.makeText(SetAccountActivity.this, "上传完成！", Toast.LENGTH_SHORT).show();
                    updateServerData();
                }
            }, null);
        }
    }

    private void checkValid() {
        nick_name = set_account_nickname.getText().toString();
        if(nick_name == null || nick_name.length() == 0)
            Toast.makeText(SetAccountActivity.this, "昵称不能为空!", Toast.LENGTH_SHORT).show();
    }

    private void updateServerData() {
        JSONObject params = new JSONObject();
        try {
            if(pngName != null)
            params.put("avatar", FrServerConfig.getQiNiuHost() + pngName);
            params.put("nick_name", nick_name);
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
        PostRequest request = new PostRequest(FrServerConfig.getUpdateUserInfoUrl(), FrApplication.getInstance().getToken(), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject res) {
                pd.dismiss();
                // 保存本地
                Author author = FrApplication.getInstance().getAuthor();
                author.setAvatar(FrServerConfig.getQiNiuHost() + pngName);
                author.setNick_name(nick_name);
                FrApplication.getInstance().setAuthor(author);
                MeFragment.isFresh = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                RequestErrorHelper.toast(SetAccountActivity.this, volleyError);
            }
        });
        FrRequest.getInstance().request(request);
    }
}

