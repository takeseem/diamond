package com.taobao.diamond.domain;

/**
 * ConfigInfo的扩展类, 解决老版本的SDK与新版本的server由于反序列化字段错误而产生的不兼容问题
 * 
 * @author leiwen.zh
 * 
 */
public class ConfigInfoEx extends ConfigInfo {

    private static final long serialVersionUID = -1L;

    // 批量查询时, 单条数据的状态码, 具体的状态码在Constants.java中
    private int status;
    // 批量查询时, 单条数据的信息
    private String message;


    public ConfigInfoEx() {
        super();
    }


    public ConfigInfoEx(String dataId, String group, String content) {
        super(dataId, group, content);
    }


    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


	@Override
	public String toString() {
		return "ConfigInfoEx [status=" + status + ", message=" + message
				+ ", getDataId()=" + getDataId() + ", getGroup()=" + getGroup()
				+ ", getContent()=" + getContent() + ", getMd5()=" + getMd5()
				+ "]";
	}

}
