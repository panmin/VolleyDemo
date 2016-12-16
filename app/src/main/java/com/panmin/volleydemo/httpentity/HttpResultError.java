package com.panmin.volleydemo.httpentity;

/**
 * Created by Administrator on 2016/2/16.
 * 服务器http返回结果的错误码
 */
public class HttpResultError {
    //http错误
    public static final int HTTP_ERROR = -1;
    //服务器返回结果中flag=0
    public static final int FLAG_ERROR = 0;
    //服务器返回结果中flag=1
    public static final int FLAG_SUCCESS = 0;
    //json格式错误
    public static final int JSON_FORMAT_ERROR = -2;
}
