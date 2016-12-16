package com.panmin.volleydemo.httpentity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;
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
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.panmin.volleydemo.LogUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * http请求控制器
 */
public class HttpController {

    private static String TAG = "HttpController";
    /**
     * Volley架包核心类
     */
    private RequestQueue mRequestQueue;

    private static int TIMEOUT = 10 * 1000; //超时时间，默认10秒
    private static int maxRetries = 1;//错误尝试次数
    private Cache mDiskBasedCache;//缓存对象
    private Network mNetwork;//网络
    private static int httpThreadCount = 5;//http线程池数量

    private static String CACHE_DIR = "/Asu/Cache";//缓存路径
    private final Map<String, String> clientHeaderMap;

    private static boolean isDebug = true;

    //缓存http请求的名称，保证一段时间内同一个名称只访问一次
    private final Queue<String> mThreadNameQueue = new ArrayBlockingQueue<>(20);

    private Context mContext;
    public HttpController(Context context) {
        this.mContext = context.getApplicationContext();
        if(mRequestQueue == null) {
            String filePath = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getPath() + CACHE_DIR : context.getFilesDir().getPath() + CACHE_DIR;
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
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
            mNetwork = new BasicNetwork(stack);
            mRequestQueue = new RequestQueue(mDiskBasedCache,mNetwork,httpThreadCount);
            //Volley.newRequestQueue(context);
            mRequestQueue.start();
        }
        clientHeaderMap = new HashMap<>();
    }

    private static HttpController mHttpController;
    public static HttpController getInstance(Context context){
        if(null == mHttpController) {
            synchronized (HttpController.class){
                if(null == mHttpController){
                    mHttpController = new HttpController(context);
                }
            }
        }
        return mHttpController;
    }



    /**
     * 取消http请求
     * @param threadName
     */
    public void cancelRequest(String threadName){
        if(mRequestQueue != null) {
            mRequestQueue.cancelAll(threadName);
        }
    }

    /**
     * 添加http请求头
     * @param header
     * @param value
     */
    public HttpController addHeader(String header, String value) {
        clientHeaderMap.put(header, value);
        return this;
    }

    /**
     * 清除http请求头
     */
    public void clearHeader() {
        clientHeaderMap.clear();
    }


    public void json2HttpResult(String json,final AjaxCallBack<String> callBack){
        /*if(isDebug) {
            LogUtil.i(TAG, "HTTP BACK:" + json);
        }
        HttpResult httpResult = null;
        try {
            httpResult = JsonUtil.json2Model(json, HttpResult.class);
        } catch (Exception e) {
            LogUtil.e(TAG, "Http back json is not json format");
            callBack.onFailure(HttpResultError.JSON_FORMAT_ERROR,e.getMessage());
            return;
        }
        if("0".equals(httpResult.getFlag())){
            String code = httpResult.getCode();
            if(TextUtils.isEmpty(code)) {
                callBack.onFailure(HttpResultError.FLAG_ERROR, httpResult.getMessage());
            }else {
                try {
                    int codeInt = Integer.valueOf(code);
                    callBack.onFailure(codeInt, httpResult.getMessage());
                }catch (Exception e){
                    LogUtil.e(TAG, "Http back flag=0 ,but code is not integer");
                    callBack.onFailure(AsuError.ERROR,e.getMessage());
                }
            }
        }else if("1".equals(httpResult.getFlag())){
            if(httpResult.getData() == null){
                callBack.onSuccess("");
                return;
            }
            String jsonData = null;
            try {
                jsonData = JsonUtil.object2Json(httpResult.getData());
            } catch (Exception e) {
                LogUtil.e(TAG,"object to jsonStr fail:"+e.getMessage());
                callBack.onFailure(AsuError.FORMAT_ERROR,e.getMessage());
            }
            if(jsonData!=null) {
                int totalSize = httpResult.getTotalSize();
                if (totalSize > 0) {
                    LogUtil.d(TAG, "http response data has totalSize");
                    callBack.onSuccess(jsonData, totalSize);
                }
                callBack.onSuccess(jsonData);
            }
        }*/
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
        if(!TextUtils.isEmpty(threadName)) {
            if (mThreadNameQueue.contains(threadName + "")) {
                callBack.onFailure(-1, "repeat http request");
                return;
            } else {
                if (mThreadNameQueue.size() == 20) {
                    mThreadNameQueue.remove();
                }
                mThreadNameQueue.add(threadName);
            }
        }
        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = clientHeaderMap;
            }
        }
        HttpRequest request = new HttpRequest(Request.Method.GET, url,headers,params,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mThreadNameQueue.remove(threadName);
                if(callBack != null){
                    json2HttpResult(response,callBack);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mThreadNameQueue.remove(threadName);
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
            timeOut = TIMEOUT;
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
        /*if(isDebug) {
            LogUtil.i(TAG, "HTTP URL:" + url+" postData:"+params.getParamString());
        }*/
        if(!TextUtils.isEmpty(threadName)) {
            if (mThreadNameQueue.contains(threadName + "")) {
                callBack.onFailure(-1, "repeat http request");
                return;
            } else {
                if (mThreadNameQueue.size() == 20) {
                    mThreadNameQueue.remove();
                }
                mThreadNameQueue.add(threadName);
            }
        }
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
                mThreadNameQueue.remove(threadName);
                if(callBack != null){
                    json2HttpResult(response,callBack);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mThreadNameQueue.remove(threadName);
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
            timeOut = TIMEOUT;
        }
        if(retry == 0){
            retry = maxRetries;
        }
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,retry,1.0f);
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }

    public void postWearLog(String url, Map<String,String> headers, AjaxParams params, final AjaxCallBack<String> callBack) {
        if(isDebug) {
            LogUtil.i(TAG, "HTTP URL:" + url);
        }

        HttpRequest request = new HttpRequest(Request.Method.POST, url,headers,params,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(callBack != null){
                    json2HttpResult(response,callBack);
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

        mRequestQueue.add(request);
    }

    public void post(String url, JSONObject jsonRequest, final AjaxCallBack<String> callBack) {
        post(url, null, jsonRequest, null, 0, 0, callBack);
    }
    public void post(String url, Map<String,String> headers, JSONObject jsonRequest, final String threadName, int timeOut, int retry, final AjaxCallBack<String> callBack) {
        if(isDebug) {
            LogUtil.i(TAG, "HTTP URL:" + url);
        }
        if(!TextUtils.isEmpty(threadName)) {
            if (mThreadNameQueue.contains(threadName + "")) {
                callBack.onFailure(-1, "repeat http request");
                return;
            } else {
                if (mThreadNameQueue.size() == 20) {
                    mThreadNameQueue.remove();
                }
                mThreadNameQueue.add(threadName);
            }
        }
        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = clientHeaderMap;
            }
        }
        HttpRequest request = new HttpRequest(url,headers,jsonRequest,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mThreadNameQueue.remove(threadName);
                if(callBack != null){
                    json2HttpResult(response,callBack);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mThreadNameQueue.remove(threadName);
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
            timeOut = TIMEOUT;
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
        if(isDebug) {
            LogUtil.i(TAG, "HTTP URL:" + url);
        }
        if(!TextUtils.isEmpty(threadName)) {
            if (mThreadNameQueue.contains(threadName + "")) {
                callBack.onFailure(-1, "repeat http request");
                return;
            } else {
                if (mThreadNameQueue.size() == 20) {
                    mThreadNameQueue.remove();
                }
                mThreadNameQueue.add(threadName);
            }
        }
        if(headers == null){
            if(clientHeaderMap.size()!=0){
                headers = clientHeaderMap;
            }
        }
        HttpRequest request = new HttpRequest(url,headers,text,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mThreadNameQueue.remove(threadName);
                if(callBack != null){
                    json2HttpResult(response,callBack);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mThreadNameQueue.remove(threadName);
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
            timeOut = TIMEOUT;
        }
        if(retry == 0){
            retry = maxRetries;
        }
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(timeOut,retry,1.0f);
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }


    //---------------------下载图片---------------------------------------
    public void downloadImage(Request request){
        mRequestQueue.add(request);
    }

}
