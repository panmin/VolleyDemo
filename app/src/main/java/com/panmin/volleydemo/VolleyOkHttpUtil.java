package com.panmin.volleydemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.panmin.volleydemo.httpentity.AjaxCallBack;
import com.panmin.volleydemo.httpentity.AjaxParams;
import com.panmin.volleydemo.httpentity.HttpRequest;
import com.panmin.volleydemo.httpentity.VolleyErrorHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * Created by panmin on 16-12-16.
 * volley和okHttp结合使用
 */

public class VolleyOkHttpUtil {

    private static final String TAG = "VolleyOkHttpUtil";
    /**
     * 需求：
     * 1. 控制超时时间
     * 2. 设置线程数目
     * 3. 能加header
     */


    private RequestQueue mRequestQueue;

    //超时时间
    private static final int TIME_OUT = 10 * 1000;

    //最大线程数
    private static final int MAX_THREAD_COUNT = 4;
    private Cache mDiskBasedCache;//缓存对象
    private Network mNetwork;//网络
    private final Map<String, String> clientHeaderMap;
    private static int maxRetries = 1;//错误尝试次数

    private Context mContext;
    public VolleyOkHttpUtil(Context context){
        this.mContext = context.getApplicationContext();
        if(mRequestQueue == null) {
            String filePath = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getPath() : context.getFilesDir().getPath() ;
            File cacheDir = new File(filePath);
            if(!cacheDir.exists()){
                cacheDir.mkdirs();
            }
            mDiskBasedCache = new DiskBasedCache(cacheDir);

            String userAgent = "asu/0";
            try {
                String packageName = context.getPackageName();
                PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
                userAgent = packageName + "/" + info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
            }
            HttpStack stack;
            /*if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }*/
            stack = new OKHttpHurlStack(new OkHttpClient());
            mNetwork = new BasicNetwork(stack);
            mRequestQueue = new RequestQueue(mDiskBasedCache,mNetwork,MAX_THREAD_COUNT);
            //Volley.newRequestQueue(context);
            mRequestQueue.start();
        }
        clientHeaderMap = new HashMap<>();
    }

    //------------------get 请求-----------------------
    public void get(String url, AjaxCallBack<String> callBack) {
        get(url, null, callBack);
    }

    public void get(String url, AjaxParams params, AjaxCallBack<String> callBack) {
        get(url, null, params, null, 0, 0, callBack);
    }
    public void get(String url, AjaxParams params,String threadName, AjaxCallBack<String> callBack) {
        get(url, null, params,threadName, 0, 0, callBack);
    }

    public void get(String url, Map<String,String> headers, AjaxParams params, final String threadName,int timeOut,int retry, final AjaxCallBack<String> callBack) {
        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = clientHeaderMap;
            }
        }
        HttpRequest request = new HttpRequest(Request.Method.GET, url,headers,params,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callBack != null){
                    //json2HttpResult(response,callBack);
                    LogUtil.i(TAG,"http back:"+response);
                    callBack.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int errNo =((error.networkResponse!=null)?error.networkResponse.statusCode:-1);
                LogUtil.e(TAG,"http get err:errNo="+errNo+" errMsg="+error.getMessage());
                //String errMsg = ((errNo==-1)?"":error.getMessage());
                String errMsg = VolleyErrorHelper.getMessage(error,mContext);//((errNo==-1)?"":error.getMessage());
                //errNo = AsuError.HttpErrorConvert(errNo);
                if(callBack != null) {
                    callBack.onFailure(errNo, errMsg);
                }else {
                    LogUtil.e(TAG,"http get AjaxCallBack is null:err="+errNo+",errMsg="+errMsg);
                }
            }
        });
        //request.setShouldCache(false);
        if(!TextUtils.isEmpty(threadName)) {
            request.setTag(threadName);
        }

        if(timeOut == 0) {
            timeOut = TIME_OUT;
        }
        if(retry == 0){
            retry = maxRetries;
        }
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,retry,1.0f);
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }



    //------------------post 请求-----------------------
    public void post(String url, AjaxParams params, AjaxCallBack<String> callBack) {
        post(url, null, params, callBack);
    }
    public void post(String url, AjaxParams params,String threadName, AjaxCallBack<String> callBack) {
        post(url, null, params,threadName,0,0, callBack);
    }

    public void post(String url, Map<String,String> headers,AjaxParams params, AjaxCallBack<String> callBack) {
        post(url, headers, params, null, 0, 0, callBack);
    }

    public void post(String url, Map<String,String> headers, AjaxParams params, final String threadName,int timeOut,int retry, final AjaxCallBack<String> callBack) {

        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = new HashMap<>();
                headers.putAll(clientHeaderMap);
            }
        }
        //headers.putAll(paramMd5(params));
        /*if(isDebug) {
            String headerStr = "";
            for (Map.Entry<String,String> entry:headers.entrySet()){
                headerStr +=entry.getKey()+"="+entry.getValue()+";";
            }
            LogUtil.i(TAG, "HTTP URL:" + url+" headers:"+headerStr+" postData:"+params.getParamString());
        }*/
        HttpRequest request = new HttpRequest(Request.Method.POST, url,headers,params,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callBack != null){
                    LogUtil.i(TAG,"http back:"+response);
                    callBack.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int errNo =((error.networkResponse!=null)?error.networkResponse.statusCode:-1);
                LogUtil.e(TAG,"http post err:errNo="+errNo+" errMsg="+error.getMessage());
                String errMsg = VolleyErrorHelper.getMessage(error,mContext);//((errNo==-1)?"":error.getMessage());
                //errNo = AsuError.HttpErrorConvert(errNo);
                if(callBack != null) {
                    callBack.onFailure(errNo, errMsg);
                }else {
                    LogUtil.e(TAG,"http post AjaxCallBack is null:err="+errNo+",errMsg="+errMsg);
                }
            }
        });
        //request.setShouldCache(false);
        if(!TextUtils.isEmpty(threadName)) {
            request.setTag(threadName);
        }
        if(timeOut == 0) {
            timeOut = TIME_OUT;
        }
        if(retry == 0){
            retry = maxRetries;
        }
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,retry,1.0f);
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }

    public void post(String url, JSONObject jsonRequest, final AjaxCallBack<String> callBack) {
        post(url, null, jsonRequest, null, 0, 0, callBack);
    }
    public void post(String url, Map<String,String> headers, JSONObject jsonRequest, final String threadName, int timeOut, int retry, final AjaxCallBack<String> callBack) {

        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = clientHeaderMap;
            }
        }
        HttpRequest request = new HttpRequest(url,headers,jsonRequest,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callBack != null){
                    LogUtil.i(TAG,"http back:"+response);
                    callBack.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int errNo =((error.networkResponse!=null)?error.networkResponse.statusCode:-1);
                LogUtil.e(TAG,"http post err:errNo="+errNo+" errMsg="+error.getMessage());
                String errMsg = VolleyErrorHelper.getMessage(error,mContext);//((errNo==-1)?"":error.getMessage());
                //String errMsg = ((errNo==-1)?"":error.getMessage());
                //errNo = AsuError.HttpErrorConvert(errNo);
                if(callBack != null) {
                    callBack.onFailure(errNo, errMsg);
                }else {
                    LogUtil.e(TAG,"http post AjaxCallBack is null:err="+errNo+",errMsg="+errMsg);
                }
            }
        });
        if(!TextUtils.isEmpty(threadName)) {
            request.setTag(threadName);
        }
        if(timeOut == 0) {
            timeOut = TIME_OUT;
        }
        if(retry == 0){
            retry = maxRetries;
        }
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,retry,1.0f);
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }

    public void post(String url, String text, final AjaxCallBack<String> callBack) {
        post(url, null, text, null, 0, 0, callBack);
    }

    public void post(String url, Map<String,String> headers, String text, final String threadName, int timeOut, int retry, final AjaxCallBack<String> callBack) {

        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = clientHeaderMap;
            }
        }
        HttpRequest request = new HttpRequest(url,headers,text,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callBack != null){
                    LogUtil.i(TAG,"http back:"+response);
                    callBack.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int errNo =((error.networkResponse!=null)?error.networkResponse.statusCode:-1);
                LogUtil.e(TAG,"http post err:errNo="+errNo+" errMsg="+error.getMessage());
                //String errMsg = ((errNo==-1)?"":error.getMessage());
                String errMsg = VolleyErrorHelper.getMessage(error,mContext);//((errNo==-1)?"":error.getMessage());
                //errNo = AsuError.HttpErrorConvert(errNo);
                if(callBack != null) {
                    callBack.onFailure(errNo, errMsg);
                }else {
                    LogUtil.e(TAG,"http post AjaxCallBack is null:err="+errNo+",errMsg="+errMsg);
                }
            }
        });
        if(!TextUtils.isEmpty(threadName)) {
            request.setTag(threadName);
        }
        if(timeOut == 0) {
            timeOut = TIME_OUT;
        }
        if(retry == 0){
            retry = maxRetries;
        }
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,retry,1.0f);
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }
}
