package com.panmin.volleydemo.httpentity;


import com.panmin.volleydemo.CallBack;
import com.panmin.volleydemo.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by panmin on 16-9-20.
 * 文件上传
 */
public class SocketRequest {
    private static final String TAG = "SocketRequest";

    /**
     *上传文件
     * @param actionUrl 接口地址
     * @param headers headers
     * @param paramsMap 参数
     * @param callBack 回调
     */
    public static void upLoadFile(String actionUrl, Map<String, String> headers, Map<String, Object> paramsMap, final CallBack<String> callBack) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();


            //创建Request
            Request.Builder post = new Request.Builder().url(actionUrl).post(body);
            //设置Headers
            for (String key : headers.keySet()) {
                post.addHeader(key,headers.get(key));
            }
            final Request request = post.build();
            //单独设置参数 比如读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(50,TimeUnit.SECONDS)
                    .build();
            final Call call =okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LogUtil.e(TAG, e.toString());
                    //failedCallBack("上传失败", callBack);
                    callBack.onFailure(null, -1,e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String string = response.body().string();
                        LogUtil.e(TAG, "response ----->" + string);
                        callBack.onSuccess(string);
                    } else {
                        callBack.onFailure(null,response.code(),response.message());
                    }
                }
            });
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
            throw e;
        }
    }

}
