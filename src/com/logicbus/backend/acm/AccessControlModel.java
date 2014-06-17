package com.logicbus.backend.acm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.anysoft.cache.Cachable;
import com.anysoft.util.JsonTools;
import com.anysoft.util.PropertiesConstants;
import com.anysoft.util.StringMatcher;
import com.anysoft.util.XmlElementProperties;
import com.anysoft.util.XmlTools;

/**
 * 访问控制模型
 * 
 * <br>
 * 访问控制模型(Access Control Model，ACM)是anyLogicBus的访问控制模型,用于控制前端对服务节点的访问权限及访问优先级.<br>
 * 
 * @author duanyy
 * @since 1.2.3
 */

public class AccessControlModel implements Cachable {
	
	/**
	 * id
	 */
	protected String id = "";
	
	/**
	 * 密码
	 */
	protected String pwd = "";
	
	/**
	 * 并发个数
	 */
	protected int maxThread = 1;
	
	/**
	 * 一分钟之内的最大访问次数
	 */
	protected int maxTimesPerMin = 10;
	
	/**
	 * 优先级
	 */
	protected int priority = -1;
	
	/**
	 * Constructor with id
	 * @param _id id
	 */
	public AccessControlModel(String _id) {
		id = _id;
	}

	public AccessControlModel(String _id,Map<String,Object> data){
		id = _id;
		fromJson(data);
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isExpired() {
		//永不过期
		return false;
	}

	@Override
	public void toXML(Element root) {
		root.setAttribute("id", id);
		root.setAttribute("pwd", pwd);
		root.setAttribute("maxThread", String.valueOf(maxThread));
		root.setAttribute("maxTimesPerMin", String.valueOf(maxTimesPerMin));
		root.setAttribute("priority", String.valueOf(priority));
		
		if (acls != null && acls.size() > 0){
			Document doc = root.getOwnerDocument();
			Element aclsElement = doc.createElement("acls");
			
			for (ACL acl:acls){
				Element aclElement = doc.createElement("acl");
				
				aclElement.setAttribute("ip", acl.ip);
				aclElement.setAttribute("service", acl.service);
				aclElement.setAttribute("maxThread", String.valueOf(acl.maxThread));
				aclElement.setAttribute("maxTimesPerMin", String.valueOf(acl.maxTimesPerMin));
				aclElement.setAttribute("priority", String.valueOf(acl.priority));
				
				aclsElement.appendChild(aclElement);
			}
			
			root.appendChild(aclsElement);
		}
		
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void toJson(Map json) {
		json.put("id", id);
		json.put("pwd", pwd);
		json.put("maxThread",String.valueOf(maxThread));
		json.put("maxTimesPerMin", String.valueOf(maxTimesPerMin));
		json.put("priority", String.valueOf(priority));
		
		if (acls != null && acls.size() > 0){
			ArrayList list = new ArrayList();
			for (ACL acl:acls){
				Map map = new HashMap();
				
				map.put("ip", acl.ip);
				map.put("service", acl.service);
				map.put("maxThread", String.valueOf(acl.maxThread));
				map.put("maxTimesPerMin", String.valueOf(acl.maxTimesPerMin));
				map.put("priority", String.valueOf(acl.priority));
				
				list.add(map);
			}
			
			json.put("acls", list);
		}
	}

	@Override
	public void fromXML(Element root) {
		XmlElementProperties props = new XmlElementProperties(root,null);
		
		pwd = PropertiesConstants.getString(props, "pwd", "");
		maxThread = PropertiesConstants.getInt(props, "maxThread", maxThread);
		maxTimesPerMin = PropertiesConstants.getInt(props, "maxTimesPerMin", maxTimesPerMin);
		priority = PropertiesConstants.getInt(props, "priority", priority);

		NodeList aclsNodeList = XmlTools.getNodeListByPath(root, "acls/acl");
		
		if (aclsNodeList.getLength() > 0){
			for (int i = 0 ,length = aclsNodeList.getLength(); i < length ; i ++){
				Node n = aclsNodeList.item(i);
				if (n.getNodeType() != Node.ELEMENT_NODE){
					continue;
				}
				
				Element e = (Element)n;
				XmlElementProperties eProps = new XmlElementProperties(e,props);
				
				ACL acl = new ACL();
				
				acl.ip = PropertiesConstants.getString(eProps, "ip", "*");
				acl.service = PropertiesConstants.getString(eProps, "service", "*");
				acl.maxThread = PropertiesConstants.getInt(eProps, "maxThread", maxThread);
				acl.maxTimesPerMin = PropertiesConstants.getInt(eProps, "maxTimesPerMin", maxTimesPerMin);
				acl.priority = PropertiesConstants.getInt(eProps, "priority", priority);
				
				acls.add(acl);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromJson(Map json) {
		pwd = JsonTools.getString(json, "pwd", "");
		maxThread = JsonTools.getInt(json, "maxThread", maxThread);
		maxTimesPerMin = JsonTools.getInt(json, "maxTimesPerMin", maxTimesPerMin);
		priority = JsonTools.getInt(json, "priority", priority);
		
		Object _acls = json.get("acls");
		if (_acls != null && _acls instanceof List){
			List _aclsList = (List) _acls;
			for (Object _acl:_aclsList){
				if (! (_acl instanceof Map)){
					continue;
				}
				Map _aclMap = (Map)_acl;
				ACL acl = new ACL();
				acl.ip = JsonTools.getString(_aclMap, "ip", "*");
				acl.service = JsonTools.getString(_aclMap, "service","*");
				acl.maxThread = JsonTools.getInt(_aclMap, "maxThread", maxThread);
				acl.maxTimesPerMin = JsonTools.getInt(_aclMap, "maxTimesPerMin", maxTimesPerMin);
				acl.priority = JsonTools.getInt(_aclMap, "priority", priority);
				acls.add(acl);
			}
		}
	}

	/**
	 * 获取本次调用的优先级
	 * @param ip 调用IP
	 * @param service 调用的服务
	 * @param stat 访问统计信息
	 * @return 优先级
	 */
	public int getPriority(String ip,String service,AccessStat stat){		
		int _maxThread = maxThread;
		int _maxTimesPerMin = maxTimesPerMin;
		int _priority = priority;
		
		ACL acl = findACL(ip,service);
		if (acl != null){
			//如果找到匹配的,则使用匹配的ACL的参数
			_maxThread = acl.maxThread;
			_maxTimesPerMin = acl.maxTimesPerMin;
			_priority = acl.priority;
		}
		
		if (stat.thread > _maxThread || stat.timesOneMin > _maxTimesPerMin){
			//如果超过并发数，或者超多一分钟调用次数
			return -1;
		}
		return _priority;
	}
	
	/**
	 * 查找匹配的ACL
	 * @param ip 
	 * @param service
	 * @return
	 */
	protected ACL findACL(String ip,String service){
		for (ACL acl:acls){
			if (acl.match(ip, service)){
				return acl;
			}
		}
		return null;
	}
	
	/**
	 * 访问控制列表
	 */
	protected Vector<ACL> acls = new Vector<ACL>();
	
	/**
	 * 访问控制列表
	 * @author duanyy
	 *
	 */
	protected static class ACL {
		public String ip;
		public String service;
		public int maxThread;
		public int maxTimesPerMin;
		public int priority;
		protected StringMatcher ipMatcher = null;
		protected StringMatcher serviceMatcher = null;
		
		/**
		 * 是否和指定的IP和服务匹配
		 * @param _ip IP地址，支持*通配符
		 * @param _service 服务，支持*通配符
		 * @return 
		 */
		public boolean match(String _ip,String _service){
			if (ipMatcher == null){
				ipMatcher = new StringMatcher(ip);
			}
			
			if (serviceMatcher == null){
				serviceMatcher = new StringMatcher(service);
			}
			
			boolean matched = ipMatcher.match(_ip);
			if (matched){
				matched = serviceMatcher.match(_service);
			}
			
			return matched;
		}
	}
}
