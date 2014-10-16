package com.logicbus.backend;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.stats.core.Dimensions;
import com.logicbus.backend.stats.core.Fragment;
import com.logicbus.backend.stats.core.Measures;
import com.logicbus.backend.stats.core.MetricsCollector;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * AccessController的实现
 * 
 * <p>本实现提供了基于SessionID的访问控制方式，提供了并发数，一分钟之内的调用次数等变量.
 * 
 * <p>本类是一个虚类，需要子类做进一步细化，包括：<br>
 * - SessionID如何组成？<br>
 * - 如何根据等待队列长度，最近一分钟之内的调用次数等变量判断访问权限<br>
 * 
 * @author duanyy
 * 
 * @version 1.0.1 [20140402 duanyy] <br>
 * - {@link com.logicbus.backend.AccessController AccessController}有更新
 * 
 * @version 1.2.1 [20140613 duanyy] <br>
 * - 共享锁由synchronized改为ReentrantLock
 * 
 * @version 1.2.8.2 [20141011 duanyy] <br>
 * - AccessStat变更可见性为public
 * - 实现Reportable和MetricsReportable
 */
abstract public class AbstractAccessController implements AccessController {
	/**
	 * 访问列表
	 */
	protected Hashtable<String,AccessStat> acl = new Hashtable<String,AccessStat>();

	/**
	 * 锁
	 */
	protected ReentrantLock lock = new ReentrantLock();
	/**
	 * 指标ID
	 */
	protected String metricsId = "acm.stat";
	
	public AbstractAccessController(Properties props){
		metricsId = PropertiesConstants.getString(props, "acm.metrics.id", metricsId);
	}
	
	@Override
	public int accessEnd(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx) {
		lock.lock();
		try {
			AccessStat current = acl.get(sessionId);
			if (current != null){
				current.thread --;
			}
		}finally{
			lock.unlock();
		}
		return 0;
	}

	@Override
	public int accessStart(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx) {
		lock.lock();
		try {
			AccessStat current = acl.get(sessionId);	
			if (current == null){
				current = new AccessStat();
				acl.put(sessionId, current);
			}
			
			current.timesTotal ++;
			current.thread ++;
			current.waitCnt = lock.getQueueLength();
			
			long timestamp = System.currentTimeMillis();
			timestamp = (timestamp / 60000)*60000;
			if (timestamp != current.timestamp){
				//新的周期
				current.timesOneMin = 1;
				current.timestamp = timestamp;
			}else{
				current.timesOneMin ++;
			}
			
			return getClientPriority(serviceId,servant,ctx,current);
		}finally{
			lock.unlock();
		}
	}
		
	/**
	 * 获取控制优先级
	 * @param serviceId 服务ID
	 * @param servant 服务描述
	 * @param ctx 上下文
	 * @param stat 当前Session的访问统计
	 * @return 优先级
	 */
	abstract protected int getClientPriority(Path serviceId,ServiceDescription servant,
			Context ctx,AccessStat stat);
	
	@Override
	public void report(Element root) {
		if (root != null){
			Document doc = root.getOwnerDocument();
			
			Enumeration<String> keys = acl.keys();
			while (keys.hasMoreElements()){
				String key = keys.nextElement();
				AccessStat value = acl.get(key);
				Element eAcl = doc.createElement("acl");
				
				eAcl.setAttribute("session", key);
				eAcl.setAttribute("currentThread", String.valueOf(value.thread));
				eAcl.setAttribute("timesTotal", String.valueOf(value.timesTotal));
				eAcl.setAttribute("timesOneMin",String.valueOf(value.timesOneMin));
				eAcl.setAttribute("waitCnt", String.valueOf(value.waitCnt));
				
				root.appendChild(eAcl);
			}
			
			root.setAttribute("module", getClass().getName());
		}
	}

	@Override
	public void report(Map<String,Object> json) {
		if (json != null){
			List<Object> acls = new ArrayList<Object>();
			
			Enumeration<String> keys = acl.keys();
			while (keys.hasMoreElements()){
				String key = keys.nextElement();
				AccessStat value = acl.get(key);
				
				Map<String,Object> mAcl = new HashMap<String,Object>();

				mAcl.put("session", key);
				mAcl.put("currentThread", String.valueOf(value.thread));
				mAcl.put("timesTotal", String.valueOf(value.timesTotal));
				mAcl.put("timesOneMin",String.valueOf(value.timesOneMin));
				mAcl.put("waitCnt", String.valueOf(value.waitCnt));
				
				acls.add(mAcl);
			}
			json.put("module", getClass().getName());
			json.put("acl", acls);
		}
	}
	
	public void report(MetricsCollector collector) {
		if (collector != null){
			Enumeration<String> keys = acl.keys();
			while (keys.hasMoreElements()){
				String key = keys.nextElement();
				AccessStat value = acl.get(key);
				
				Fragment f = new Fragment(metricsId);
				
				Dimensions dims = f.getDimensions();
				if (dims != null)
					dims.lpush(key);
				
				Measures meas = f.getMeasures();
				if (meas != null)
					meas.lpush(new Object[]{
							value.thread,
							value.timesTotal,
							value.timesOneMin,
							value.waitCnt
					});
				
				collector.metricsIncr(f);
			}			
		}
	}
	
	/**
	 * 访问统计
	 * @author duanyy
	 *
	 */
	public static class AccessStat {
		/**
		 * 总调用次数
		 */
		public long timesTotal = 0;
		/**
		 * 最近一分钟调用次数
		 */
		public int timesOneMin = 0;
		/**
		 * 当前接入进程个数
		 */
		public int thread = 0;
		/**
		 * 时间戳(用于定义最近一分钟)
		 */
		public long timestamp = 0;
		
		/**
		 * 等待进程数
		 * @since 1.2.1
		 */
		public int waitCnt = 0;
	}
}
