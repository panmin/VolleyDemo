package com.panmin.volleydemo;

/**
 * Created by Administrator on 2015/10/8.
 * 异步返回数据
 */
public abstract class CallBack<T> {
    private int rate = 1000 * 1;//每秒
    public int getRate() {
        return rate;
    }
    public void onStart(){}
    public void onLoading(long count,long current){}
    public void onSuccess(T t){}
    public void onSuccess(T t,int totalSize){}
    public void onFailure(Throwable t,int errorNo ,String strMsg){}
}
