package com.logicbus.dbcp.jndi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.stats.core.MetricsCollector;
import com.logicbus.dbcp.core.ConnectionPool;
import com.logicbus.dbcp.sql.SQLTools;
import com.logicbus.dbcp.util.ConnectionPoolStat;

/**
 * 基于JNDI DataSource的连接池
 * 
 * @author duanyy
 * 
 * @since 1.2.9
 * 
 * @version 1.2.9.1 [20141017 duanyy]
 * - ConnectionPoolStat模型更新
 */
public class JndiConnectionPool implements ConnectionPool {
	protected static final Logger logger = LogManager.getLogger(JndiConnectionPool.class);
	protected String name;
	protected DataSource datasource = null;
	protected ConnectionPoolStat stat = null;
	public JndiConnectionPool(String _name,DataSource _ds){
		name = _name;
		datasource = _ds;
	}
	
	@Override
	public Connection getConnection(int timeout,boolean enableRWS) {
		Connection conn = null;
		if (datasource != null){
			long start = System.currentTimeMillis();
			try {
				return datasource.getConnection();
			} catch (SQLException e) {
				logger.error("Error when getting jdbc connection from datasource:" + name,e);
			}finally{
				if (stat != null){
					stat.count(System.currentTimeMillis() - start, conn == null);
				}
			}
		}
		return conn;
	}

	@Override
	public Connection getConnection(int timeout) {
		return getConnection(3000,false);
	}

	@Override
	public Connection getConnection(boolean enableRWS) {
		return getConnection(3000,enableRWS);
	}

	@Override
	public Connection getConnection() {
		return getConnection(3000,false);
	}
	
	@Override
	public void recycle(Connection conn) {
		SQLTools.close(conn);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void report(Element root) {
		if (root != null){
			root.setAttribute("module", JndiConnectionPool.class.getName());
			
			Document doc = root.getOwnerDocument();
			// runtime
			{
				Element _runtime = doc.createElement("runtime");
				
				if (stat != null){
					Element _stat = doc.createElement("stat");
					stat.report(_stat);			
					_runtime.appendChild(_stat);
				}
				
				root.appendChild(_runtime);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			json.put("module", JndiConnectionPool.class.getName());
			
			// runtime
			{
				Map<String,Object> _runtime = new HashMap<String,Object>();
				
				if (stat != null){
					Map<String,Object> _stat = new HashMap<String,Object>();
					stat.report(_stat);			
					_runtime.put("stat", _stat);
				}
				json.put("runtime",_runtime);
			}
		}
	}

	@Override
	public void report(MetricsCollector collector) {
		
	}

	@Override
	public void close() throws Exception {
	}


}
