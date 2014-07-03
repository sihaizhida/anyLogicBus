package com.logicbus.backend;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServantManager;
import com.logicbus.models.servant.ServiceDescription;
import com.logicbus.models.servant.ServiceDescriptionWatcher;

/**
 * 基于QueuedPool的ServantFactory
 * 
 * @author duanyy
 * @since 1.2.4
 * 
 */
public class QueuedServantFactory implements ServiceDescriptionWatcher {
	/**
	 * a logger of log4j
	 */
	protected Logger logger = LogManager.getLogger(QueuedServantFactory.class);
	/**
	 * 服务资源池列表
	 */
	private Hashtable<String, QueuedServantPool> m_pools = null;
	
	/**
	 * constructor
	 */
	protected QueuedServantFactory(){
		ServantManager sm = ServantManager.get();
		sm.addWatcher(this);
		m_pools = new Hashtable<String, QueuedServantPool>();
	}
	
	/**
	 * 获得服务资源池列表
	 * @return 服务资源池列表
	 */
	public QueuedServantPool [] getPools(){
		return m_pools.values().toArray(new QueuedServantPool[0]);
	}
	
	/**
	 * 获取指定服务的服务资源池
	 * @param id 服务ID
	 * @return 服务资源池
	 * @throws ServantException 当没有找到服务定义时抛出
	 */
	protected QueuedServantPool getServantPool(Path id)throws ServantException
	{
		ServantManager sm = ServantManager.get();
		ServiceDescription sd = sm.get(id);
		if (sd == null){
			throw new ServantException("core.service_not_found","No service desc is found:" + id);
		}
		
		return new QueuedServantPool(sd);		
	}
	
	
	/**
	 * 重新装入指定服务的资源池
	 * @param _id 服务id
	 * @return 服务资源池
	 * @throws ServantException
	 */
	public QueuedServantPool reloadPool(Path _id) throws ServantException{
		lockPools.lock();
		try {
			QueuedServantPool temp = m_pools.get(_id.getPath());
			if (temp != null){
				//重新装入的目的是因为更新了服务描述信息			
				ServantManager sm = ServantManager.get();
				ServiceDescription sd = sm.get(_id);
				temp.reload(sd);
			}
			return temp;
		}finally{
			lockPools.unlock();
		}
	}
	
	/**
	 * 获取指定服务的的服务资源池
	 * @param _id 服务Id
	 * @return 服务资源池
	 * @throws ServantException
	 * @see {@link #getServantPool(String)}
	 */
	public QueuedServantPool getPool(Path _id) throws ServantException{
		Object found = m_pools.get(_id.getPath());
		if (found != null){
			return (QueuedServantPool)found;
		}
		lockPools.lock();
		try {
			Object temp = m_pools.get(_id.getPath());
			if (temp != null){		
				QueuedServantPool pool = (QueuedServantPool)temp;
				return pool;
			}

			QueuedServantPool newPool = getServantPool(_id);
			if (newPool != null)
			{
				m_pools.put(_id.getPath(), newPool);
				return newPool;
			}
			return null;
		}finally{
			lockPools.unlock();
		}
	}
	
	/**
	 * m_pools对象锁
	 */
	protected ReentrantLock lockPools = new ReentrantLock();
	
	/**
	 * 关闭
	 */
	public void close(){
		lockPools.lock();
		try {
			Enumeration<QueuedServantPool> pools = m_pools.elements();
			
			while (pools.hasMoreElements()){
				QueuedServantPool sp = pools.nextElement();
				if (sp != null){
					sp.close();
				}
			}
		}finally{
			lockPools.unlock();
		}
	}
	
	/**
	 * 唯一实例
	 */
	protected static QueuedServantFactory instance = null;
	
	static {
		instance = new QueuedServantFactory();
	}
	
	/**
	 * 获取唯一实例
	 * @return 唯一实例
	 */
	public static QueuedServantFactory get(){
		return instance;
	}

	@Override
	public void changed(Path id, ServiceDescription desc) {
		lockPools.lock();
		try {
			logger.info("changed" + id);
			QueuedServantPool temp = m_pools.get(id);
			if (temp != null){
				//重新装入的目的是因为更新了服务描述信息			
				logger.info("Service has been changed,reload it:" + id);
				temp.reload(desc);
			}
		}finally{
			lockPools.unlock();
		}
	}
	
	@Override
	public void removed(Path id){
		lockPools.lock();
		try {
			logger.info("removed:" + id);
			QueuedServantPool temp = m_pools.get(id);
			if (temp != null){
				//服务被删除了
				logger.info("Service has been removed,close it:" + id);
				temp.close();
				m_pools.remove(id);
			}
		}finally{
			lockPools.unlock();
		}		
	}
}