package com.logicbus.dbcp.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.loadbalance.LoadBalance;
import com.anysoft.loadbalance.LoadBalanceFactory;
import com.anysoft.pool.QueuedPool2;
import com.anysoft.util.Counter;
import com.anysoft.util.KeyGen;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.metrics.core.MetricsCollector;
import com.logicbus.dbcp.context.DbcpSource;
import com.logicbus.dbcp.core.ConnectionPool;
import com.logicbus.dbcp.util.ConnectionPoolStat;


/**
 * ConnectionPool 虚基类
 * @author duanyy
 * 
 * @since 1.2.9
 * 
 * @version 1.2.9.1 [20141017 duanyy]
 * - ConnectionPool有更新
 * - 实现Reportable接口
 * - ConnectionPoolStat模型更新
 * 
 * @version 1.2.9.3 [20141022 duanyy]
 * - 增加对读写分离的支持
 */
abstract public class AbstractConnectionPool extends QueuedPool2<Connection> implements ConnectionPool{
	protected Counter stat = null;
	protected LoadBalance<ReadOnlySource> loadBalance = null;
	
	@Override
	public void create(Properties props){
		boolean enableStat = true;
		
		enableStat = PropertiesConstants.getBoolean(props, "dbcp.stat.enable", enableStat);
		
		if (enableStat){
			stat = createCounter(props);
		}else{
			stat = null;
		}
		
		//loadbalance
		{
			String lbModule = props.GetValue("loadbalance.module", "Rand");
			
			LoadBalanceFactory<ReadOnlySource> f = new LoadBalanceFactory<ReadOnlySource>();
			
			loadBalance = f.newInstance(lbModule, props);
		}
	}
	
	protected Counter createCounter(Properties p){
		String module = PropertiesConstants.getString(p,"dbcp.stat.module", ConnectionPoolStat.class.getName());
		try {
			return Counter.TheFactory.getCounter(module, p);
		}catch (Exception ex){
			logger.warn("Can not create dbcp counter:" + module + ",default counter is instead.");
			return new ConnectionPoolStat(p);
		}
	}
	
	@Override
	public void report(Element xml) {
		if (xml != null){
			Document doc = xml.getOwnerDocument();
			
			//pool
			{
				Element _pool = doc.createElement("pool");
				super.report(_pool);
				xml.appendChild(_pool);
			}
			
			// stat
			if (stat != null){
				Element _stat = doc.createElement("stat");
				super.report(_stat);
				xml.appendChild(_stat);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			//pool
			{
				Map<String,Object> _pool = new HashMap<String,Object>();
				super.report(_pool);
				json.put("pool", _pool);
			}
			
			if (stat != null){
				Map<String,Object> _stat = new HashMap<String,Object>();
				super.report(_stat);
				json.put("stat", _stat);
			}
		}
	}

	@Override
	public void report(MetricsCollector collector) {
		// to be define
	}

	@Override
	public Connection getConnection(int timeout, boolean enableRWS) {
		Connection conn = null;
		if (enableRWS){
			conn = selectReadSource(timeout);
		}
		
		if (conn == null){
			long start = System.currentTimeMillis();
			try {
				int _timeout = timeout > getMaxWait() ? getMaxWait() : timeout;
				conn = borrowObject(0,_timeout);			
			}finally{
				if (stat != null){
					stat.count(System.currentTimeMillis() - start, conn == null);
				}
			}
		}
		return conn;
	}
	
	/**
	 * 尝试选择只读数据源
	 * 
	 * @return
	 */
	protected Connection selectReadSource(int timeout){
		Connection found = null;
		
		List<ReadOnlySource> ross = getReadOnlySources();
		if (ross != null && ross.size() > 0 && loadBalance != null){
			long start = System.currentTimeMillis();
			boolean error = false;
			ReadOnlySource dest = null;
			try {	
				dest = loadBalance.select(KeyGen.getKey(), null, ross);
				
				ConnectionPool pool = DbcpSource.getPool(dest.getId());
				if (pool != null){
					found = pool.getConnection(timeout);
				}
			} catch (Exception e) {
				error = true;
			}finally{
				long _duration = System.currentTimeMillis() - start;
				if (dest != null){
					dest.count(_duration, error);
				}
			}
		}
		return found;
	}
	
	abstract protected List<ReadOnlySource> getReadOnlySources();

	@Override
	public Connection getConnection(int timeout) {
		return getConnection(timeout,false);
	}

	@Override
	public Connection getConnection(boolean enableRWS) {
		return getConnection(getMaxWait(),enableRWS);
	}

	@Override
	public Connection getConnection() {
		return getConnection(getMaxWait(),false);
	}
	
	@Override
	public void recycle(Connection conn) {
		if (conn != null)
			returnObject(conn);
	}
	
	@Override
	protected String getIdOfMaxQueueLength() {
		return "maxActive";
	}

	@Override
	protected String getIdOfIdleQueueLength() {
		return "maxIdle";
	}
	
	/**
	 * 获取争抢连接时的最大等待时间
	 * @return
	 */
	abstract protected int getMaxWait();
}
