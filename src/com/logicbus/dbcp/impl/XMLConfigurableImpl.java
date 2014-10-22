package com.logicbus.dbcp.impl;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.XMLConfigurable;
import com.anysoft.util.XmlElementProperties;

/**
 * 可通过XML配置的缓冲池实现
 * 
 * @author duanyy
 * @version 1.2.9.3 [20141122 duanyy]
 * - 增加对读写分离的支持
 * 
 */
public class XMLConfigurableImpl extends AbstractConnectionPool implements XMLConfigurable{

	@Override
	public String getName() {
		return model.getName();
	}

	@Override
	public void configure(Element _e, Properties _properties)
			throws BaseException {
		Properties p = new XmlElementProperties(_e,_properties);
		
		model = new ConnectionModel();
		model.fromXML(_e);
		
		create(p);
	}

	@Override
	public void report(Element xml) {
		if (xml != null){
			model.report(xml);
			
			Document doc = xml.getOwnerDocument();
			
			// runtime
			{
				Element _runtime = doc.createElement("runtime");
				super.report(_runtime);
				xml.appendChild(_runtime);
			}
		}
	}

	@Override
	public void report(Map<String, Object> json) {
		if (json != null){
			model.report(json);
			
			// runtime
			{
				Map<String,Object> _runtime = new HashMap<String,Object>();
				super.report(_runtime);
				json.put("runtime", _runtime);
			}
		}
	}
	
	@Override
	protected int getMaxWait() {
		return model.getMaxWait();
	}

	@Override
	protected Connection createObject() throws BaseException {
		return model.newConnection();
	}
	
	protected ConnectionModel model;

	@Override
	protected List<ReadOnlySource> getReadOnlySources() {
		return model.getReadOnlySources();
	}


}