package com.panmin.volleydemo;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by panmin on 16-12-15.
 * okHttp的get方式请求
 */

public class OKHttpGetTest {
    private static final String TAG = "OKHttpGetTest";

    @Test
    public void getString() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        Response response = okHttpClient.newCall(request).execute();//同步get
        if(response.isSuccessful()){
            System.out.print(response.body().string());
        }
        Assert.assertEquals(response.isSuccessful(),true);
    }


    @Test
    public void getScreamSync() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
                Assert.fail();
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //System.out.println(response.body().string());
                InputStream inputStream = response.body().byteStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] bytes = new byte[1];
                while (inputStream.read(bytes)!=-1) {
                    outputStream.write(bytes);
                }
                String str = new String(outputStream.toByteArray());
                System.out.println(str);
                outputStream.close();
                inputStream.close();
                latch.countDown();
            }
        });
        latch.await();
    }

}
