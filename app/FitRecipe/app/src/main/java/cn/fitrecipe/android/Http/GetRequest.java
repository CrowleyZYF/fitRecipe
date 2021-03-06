package cn.fitrecipe.android.Http;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wk on 2015/7/23.
 */
public class GetRequest extends JsonObjectRequest{

    private String token;
    private final int timeout = 10000;
    private final int retries = 0;

    public GetRequest(String url, String token, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, jsonRequest, listener, errorListener);
        this.token = token;
        this.setRetryPolicy(new DefaultRetryPolicy(timeout, retries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        System.out.println(Calendar.getInstance().getTime() + "Method:GET Url:" + url + " Authorization: Token " +token + " params: " + jsonRequest.toString());
    }

    public GetRequest(String url, String token, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, listener, errorListener);
        this.token = token;
        this.setRetryPolicy(new DefaultRetryPolicy(timeout, retries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        System.out.println(Calendar.getInstance().getTime() + "Method:GET Url:" + url + " Authorization: Token " + token + " params: null");
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        if(token != null && token.length() > 0) {
            headers.put("Authorization", "Token " + token);
        }
        return headers;
    }




}
