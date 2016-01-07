package com.taobao.diamond.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 批处理的返回结果
 * 
 * @author leiwen.zh
 * 
 */
public class BatchContextResult<T> implements Serializable {

    private static final long serialVersionUID = -5170746311067772091L;

    // 批处理是否成功
    private boolean success = true;
    // 请求返回的状态码
    private int statusCode;
    // 用户可读的返回信息
    private String statusMsg;
    // response中的元信息
    private String responseMsg;
    // 返回的结果集
    private List<T> result;


    public BatchContextResult() {
        this.result = new ArrayList<T>();
    }


    public boolean isSuccess() {
        return success;
    }


    public void setSuccess(boolean success) {
        this.success = success;
    }


    public int getStatusCode() {
        return statusCode;
    }


    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


    public String getStatusMsg() {
        return statusMsg;
    }


    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }


    public String getResponseMsg() {
        return responseMsg;
    }


    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }


    public List<T> getResult() {
        return result;
    }


    @Override
    public String toString() {
        return "BatchContextResult [success=" + success + ", statusCode=" + statusCode + ", statusMsg=" + statusMsg
                + ", result=" + result + "]";
    }

}
