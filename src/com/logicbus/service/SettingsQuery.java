package com.logicbus.service;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Settings;
import com.logicbus.backend.AbstractServant;
import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.JsonMessage;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.servant.ServiceDescription;


/**
 * 查询当前的Settings
 * 
 * @author duanyy
 * 
 * @since 1.2.9.1
 *
 */
public class SettingsQuery extends AbstractServant {

	@Override
	protected void onDestroy() {

	}

	@Override
	protected void onCreate(ServiceDescription sd) throws ServantException {

	}

	@Override
	protected int onXml(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);		
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		//创捷worker节点
		Element eSettings = doc.createElement("settings");
		{
			Settings settings = Settings.get();
			settings.report(eSettings);
			root.appendChild(eSettings);
		}
		return 0;
	}

	@Override
	protected int onJson(MessageDoc msgDoc, Context ctx) throws Exception {
		JsonMessage msg = (JsonMessage)msgDoc.asMessage(JsonMessage.class);		
		Map<String,Object> root = msg.getRoot();
		
		
		Map<String,Object> _settings = new HashMap<String,Object>();
		
		Settings settings = Settings.get();
		settings.report(_settings);
		
		root.put("settings", _settings);
		
		return 0;
	}

}
