package com.logicbus.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.anysoft.util.JsonSerializer;
import com.anysoft.util.JsonTools;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlSerializer;


/**
 * 数据库连接工厂类
 * 
 * <br>
 * 负责根据配置信息创建连接
 * 
 * @author duanyy
 * @since 1.0.6
 */
public class ConnectionFactory implements XmlSerializer,JsonSerializer{
	/**
	 * a logger of log4j
	 */
	protected static final Logger logger = LogManager.getLogger(ConnectionFactory.class);	
	/**
	 * 名称
	 */
	protected String name;
	
	/**
	 * 获取名称
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * jdbc驱动
	 */
	protected String driver;
	
	/**
	 * 获取JDBC驱动类
	 * @return
	 */
	public String getDriver(){
		return driver;
	}
	
	/**
	 * URL
	 */
	protected String url;
	
	/**
	 * 获取数据库连接所用的URI
	 * @return
	 */
	public String getURI(){
		return url;
	}
	
	/**
	 * username
	 */
	protected String username;
	
	/**
	 * 获取用户名
	 * @return
	 */
	public String getUserName(){
		return username;
	}
	
	/**
	 * password
	 */
	protected String password;
	
	/**
	 * 获取密码
	 * @return
	 */
	public String getPassword(){
		return password;
	}
	
	/**
	 * 最大连接数
	 */
	protected int maxActive = 3;
	
	/**
	 * 获取maxActive
	 * @return
	 */
	public int getMaxActive(){return maxActive;}
	
	/**
	 * 空闲连接数
	 */
	protected int maxIdle = 1;
	
	/**
	 * 获取空闲连接数
	 * @return
	 */
	public int getMaxIdle(){return maxIdle;}
	
	/**
	 * 空闲连接数
	 */
	protected int maxWait = 5000;
	
	/**
	 * 获取最大等待时间
	 * @return
	 */
	public int getMaxWait(){return maxWait;}
	
	/**
	 * 监控指标
	 */
	protected String monitor;
	
	/**
	 * 获得监控指标
	 * @return
	 */
	public String getMonitor(){
		return monitor;
	}
	
	/**
	 * 按照当前的连接属性创建数据库连接
	 * @return
	 */
	public Connection newConnection(){
		Connection conn = null;
		try {
			ClassLoader cl = getClassLoader();
			cl.loadClass(driver);
			conn = DriverManager.getConnection(url, username, password);
		}catch (Exception ex){
			logger.error("Can not create a connection to " + url,ex);
		}
		
		return conn;
	}

	
	/**
	 * 获取当前的ClassLoader
	 * @return
	 */
	protected ClassLoader getClassLoader(){
		Settings settings = Settings.get();
		
		ClassLoader cl = (ClassLoader) settings.get("classLoader");
		if (cl == null){
			cl = Thread.currentThread().getContextClassLoader();
		}
		
		return cl;
	}
	
	public void report(Element e){
		e.setAttribute("name", name);
		e.setAttribute("driver", driver);
		e.setAttribute("url", url);
		e.setAttribute("username", username);
		e.setAttribute("password", "********");
		e.setAttribute("maxActive", String.valueOf(maxActive));
		e.setAttribute("maxIdle", String.valueOf(maxIdle));
		e.setAttribute("maxWait", String.valueOf(maxWait));
		e.setAttribute("monitor", monitor);		
	}
	
	@Override
	public void toXML(Element e) {
		e.setAttribute("name", name);
		e.setAttribute("driver", driver);
		e.setAttribute("url", url);
		e.setAttribute("username", username);
		e.setAttribute("password", password);
		e.setAttribute("maxActive", String.valueOf(maxActive));
		e.setAttribute("maxIdle", String.valueOf(maxIdle));
		e.setAttribute("maxWait", String.valueOf(maxWait));
		e.setAttribute("monitor", monitor);
	}

	@Override
	public void fromXML(Element e) {
		XmlElementProperties props = new XmlElementProperties(e,null);
		
		name = PropertiesConstants.getString(props,"name", "");
		driver = PropertiesConstants.getString(props, "driver", "");
		url = PropertiesConstants.getString(props, "url", "");
		username = PropertiesConstants.getString(props, "username","");
		password = PropertiesConstants.getString(props, "password","");
		maxActive = PropertiesConstants.getInt(props, "maxActive",3);
		maxIdle = PropertiesConstants.getInt(props, "maxIdle",1);
		maxWait = PropertiesConstants.getInt(props, "maxWait",5000);
		monitor = PropertiesConstants.getString(props, "monitor", "");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void toJson(Map json) {
		JsonTools.setString(json, "name",name);
		JsonTools.setString(json, "driver",driver);
		JsonTools.setString(json, "url",url);
		JsonTools.setString(json, "username", username);
		JsonTools.setString(json, "password",password);
		JsonTools.setInt(json, "maxActive", maxActive);
		JsonTools.setInt(json, "maxIdle", maxIdle);
		JsonTools.setInt(json, "maxWait", maxWait);
		JsonTools.setString(json, "monitor", monitor);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromJson(Map json) {
		name = JsonTools.getString(json, "name", "");
		driver = JsonTools.getString(json, "driver", "");
		url = JsonTools.getString(json, "url", "");
		username = JsonTools.getString(json, "username", "");
		password = JsonTools.getString(json, "password", "");
		maxActive = JsonTools.getInt(json, "maxActive",3);
		maxIdle = JsonTools.getInt(json, "maxIdle",1);
		maxWait = JsonTools.getInt(json, "maxWait",5000);
		monitor = JsonTools.getString(json, "monitor", "");
	}
}
