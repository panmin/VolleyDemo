package com.panmin.volleydemo.httpentity;

/**
 * Created by Administrator on 2016/2/16.
 * http返回的结果格式
 */
public class HttpResult {

    /**
     * message : 成功!
     * flag : 1
     * data : {}
     * code :
     */

    /**
     * 返回信息
     */
    private String message;

    /**
     * 1:成功,0:失败
     */
    private String flag;

    /**
     * 返回结果数据
     */
    private Object data;

    /**
     * 预留处理编码
     */
    private String code;

    private int totalSize;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public String getFlag() {
        return flag;
    }

    public Object getData() {
        return data;
    }

    public String getCode() {
        return code;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}
