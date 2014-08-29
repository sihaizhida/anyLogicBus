package com.logicbus.backend.bizlog;

import java.lang.reflect.Field;

import com.anysoft.stream.Flowable;

/**
 * 日志项
 * 
 * @author duanyy
 * 
 * @since 1.2.3
 * 
 * @version 1.2.7 [20140828 duanyy] <br>
 * - 通过com.anysoft.stream来实现
 */
public class BizLogItem implements Comparable<BizLogItem>,Flowable {
	
	/**
	 * 全局序列号
	 */
	public String sn;
	
	/**
	 * 服务ID
	 */
	public String id;
	
	/**
	 * 调用者
	 */
	public String client;
	
	/**
	 * 调用者IP
	 */
	public String clientIP;
	
	/**
	 * 服务主机
	 */
	public String host;
	
	/**
	 * 结果代码
	 */
	public String result;
	
	/**
	 * 结果原因
	 */
	public String reason;
	
	/**
	 * 开始时间
	 */
	public long startTime;
	
	/**
	 * 服务时长
	 */
	public long duration;
	
	/**
	 * 请求URL
	 */
	public String url;
	
	/**
	 * 服务文档内容
	 */
	public String content;
	
	@Override
	public int compareTo(BizLogItem other) {		
		return sn.compareTo(other.sn);
	}

	@Override
	public String getValue(String varName, Object context, String defaultValue) {
		try {
			Class<?> clazz = this.getClass();
			Field field = clazz.getField(varName);
			if (field == null){
				return defaultValue;
			}
			
			Object found = field.get(this);
			return found.toString();
		}catch (Exception ex){
			return defaultValue;
		}
	}

	@Override
	public Object getContext(String varName) {
		return null;
	}

	@Override
	public String getStatsDimesion() {
		return client + "%" + id + "%" + result;
	}	
	
	@Override
	public int hashCode(){
		return sn.hashCode();
	}
}
