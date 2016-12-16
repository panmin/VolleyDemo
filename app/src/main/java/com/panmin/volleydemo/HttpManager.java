package com.panmin.volleydemo;

import android.content.Context;

import com.panmin.volleydemo.httpentity.HttpController;

import java.util.TimeZone;

/**
 * Created by Administrator on 2016/1/13.
 */
public class HttpManager {

    private static final String TAG = "HttpManager";
    //是否已初始化
    private boolean isInit = false;
    //单例对象
    private static HttpManager instance;
    //上下文
    private Context mContext;

    private HttpManager(Context context) {
        this.mContext = context;
        init(context);
    }

    /**
     * 获取单例对象
     *
     * @return
     */
    public static HttpManager getInstance(Context context) {
        if (null == instance) {
            synchronized (HttpManager.class) {
                if (null == instance)
                    instance = new HttpManager(context.getApplicationContext());
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(Context context) {

    }

    public HttpController getHttp(){
        HttpController http = HttpController.getInstance(mContext);
        //http.addHeader("phoneId", AppTools.getDeviceId(mContext));
        //http.addHeader("type", "1");
        //http.addHeader("phone", AsuApplication.getInstance().getUserName());
        //http.addHeader("version", AppTools.getVersion(mContext));
        //String token = AsuApplication.getInstance().getToken();
        //http.addHeader("tokenValue", token);
        //if(TextUtils.isEmpty(token)){
        //    LogUtil.e(TAG,"TOKEN="+token);
        //}
        http.addHeader("timeZone", TimeZone.getDefault().getID());
        return http;
    }

    public VolleyOkHttpUtil getOkHttp(){
        VolleyOkHttpUtil http = new VolleyOkHttpUtil(mContext);

        return http;
    }

}
