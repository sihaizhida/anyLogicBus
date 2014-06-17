package com.logicbus.backend.acm;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.AccessController;
import com.logicbus.backend.Context;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 基于ACM的访问控制器
 * 
 * @author duanyy
 * @since 1.2.3
 */
abstract public class ACMAccessController implements AccessController {
	/**
	 * 访问列表
	 */
	protected Hashtable<String,AccessStat> acl = new Hashtable<String,AccessStat>();

	/**
	 * 锁
	 */
	protected ReentrantLock lock = new ReentrantLock();
	
	/**
	 * ACM缓存管理器
	 */
	protected ACMCacheManager acmCache = null;
	
	public ACMAccessController(){
		acmCache = getCacheManager();
	}
	
	/**
	 * 创建CacheManager
	 * @return
	 */
	protected ACMCacheManager getCacheManager(){
		return ACMCacheManager.get();
	}
	
	protected String getACMObject(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx){
		return sessionId + ":" + serviceId.getPath();
	}
	
	@Override
	public int accessStart(String sessionId,Path serviceId, ServiceDescription servant,
			Context ctx) {
		AccessControlModel acm = acmCache.load(sessionId);
		if (acm == null){
			//-2表明无法为当前接入找到访问控制模型
			return -2;
		}
		
		lock.lock();
		try{
			String acmObject = getACMObject(sessionId,serviceId,servant,ctx);
			AccessStat current = acl.get(acmObject);	
			if (current == null){
				current = new AccessStat();
				acl.put(acmObject, current);
			}
			
			current.timesTotal ++;
			current.thread ++;
			
			long timestamp = System.currentTimeMillis();
			timestamp = (timestamp / 60000)*60000;
			if (timestamp != current.timestamp){
				//新的周期
				current.timesOneMin = 1;
				current.timestamp = timestamp;
			}else{
				current.timesOneMin ++;
			}
			
			return acm.getPriority(ctx.getClientIp(), serviceId.getPath(), current);
		}finally{
			lock.unlock();
		}
	}

	@Override
	public int accessEnd(String sessionId,Path serviceId, ServiceDescription servant, Context ctx) {
		lock.lock();
		try{
			String acmObject = getACMObject(sessionId,serviceId,servant,ctx);
			AccessStat current = acl.get(acmObject);
			if (current != null){
				current.thread --;
			}
		}finally{
			lock.unlock();
		}
		return 0;
	}

	@Override
	public void toXML(Element root) {
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
	}

}
