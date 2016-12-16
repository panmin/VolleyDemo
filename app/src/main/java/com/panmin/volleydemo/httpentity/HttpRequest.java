package com.panmin.volleydemo.httpentity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.panmin.volleydemo.LogUtil;

import org.apache.http.HttpEntity;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/14.
 */
public class HttpRequest extends Request<String> {
    private static final String TAG = "HttpRequest";
    private final Listener<String> mListener;

    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";
    private static boolean isDebug = true;

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private int mMethod;
    private String mUrl;
    private Map<String,String> mHeaders;
    private AjaxParams mParams;
    private HttpEntity mHttpEntity;
    private String mRequestBody;
    /**
     * Creates a new request with the given method.
     *
     * @param method        the request {@link Method} to use
     * @param url           URL to fetch the string at
     * @param headers       HTTP headers
     * @param params        HTTP params
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public HttpRequest(int method, String url,Map<String,String> headers,JSONObject jsonRequest, AjaxParams params, Listener<String> listener,
                         Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mMethod = method;
        this.mUrl = url;
        this.mHeaders = headers;
        this.mListener = listener;
        if(params != null) {
            this.mParams = params;
            mHttpEntity = paramsToEntity(params);
        }else {
            mRequestBody =((jsonRequest == null) ? null : jsonRequest.toString());
        }
    }
    public HttpRequest(int method, String url,Map<String,String> headers, AjaxParams params, Listener<String> listener,
                       Response.ErrorListener errorListener) {
        this(method, url, headers,null, params, listener, errorListener);
    }

    public HttpRequest(String url,Map<String,String> headers, JSONObject jsonRequest,
                             Listener<String> listener, Response.ErrorListener errorListener) {
        this(Method.POST,url,headers,jsonRequest,null,listener,errorListener);
    }
    public HttpRequest(String url,Map<String,String> headers, String text,
                       Listener<String> listener, Response.ErrorListener errorListener) {
        this(Method.POST,url,headers,null,null,listener,errorListener);
        mRequestBody = text;
    }

    @Override
    public String getUrl() {
        if (mParams == null) {
            return super.getUrl();
        } else {
            if(mMethod == Method.GET){
                return getUrlWithQueryString(mUrl, mParams);
            }else {
                return super.getUrl();
            }
        }
    }
    public static String getUrlWithQueryString(String url, AjaxParams params) {
        if(params != null) {
            String paramString = params.getParamString();
            url += "?" + paramString;
        }
        return url;
    }
    private HttpEntity paramsToEntity(AjaxParams params) {
        HttpEntity entity = null;

        if(params != null) {
            entity = params.getEntity();
        }

        return entity;
    }

    /**
     * Creates a new GET request.
     *
     * @param url           URL to fetch the string at
     * @param headers       HTTP headers
     * @param params        HTTP params
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public HttpRequest(String url,Map<String,String> headers,AjaxParams params, Listener<String> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, headers,null, params, listener, errorListener);
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(null == mHeaders){
            return super.getHeaders();
        }else {
            if(isDebug) {
                if(mParams!=null) {
                    String headerStr = "";
                    for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                        headerStr += entry.getKey() + "=" + entry.getValue() + ";";
                    }
                    LogUtil.i(TAG, "HTTP URL:" + mUrl + " headers:" + headerStr + " postData:" + mParams.getParamString());
                }
            }
            return mHeaders;
        }
    }

    @Override
    public String getBodyContentType() {
        if(mParams == null){
            return PROTOCOL_CONTENT_TYPE;
        }
        if(mHttpEntity == null) {
            return super.getBodyContentType();
        }else {
            return mHttpEntity.getContentType().getValue();
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(mParams == null){
            try {
                return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
            } catch (UnsupportedEncodingException uee) {
                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                        mRequestBody, PROTOCOL_CHARSET);
                return null;
            }
        }
        if(mHttpEntity == null) {
            return super.getBody();
        }else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                mHttpEntity.writeTo(bos);
                bos.flush();
            } catch (IOException e) {
                LogUtil.e(TAG, "IOException writing to ByteArrayOutputStream");
            }finally {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bos.toByteArray();
        }
    }
}
