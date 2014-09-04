package com.logicbus.service;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.anysoft.util.Settings;
import com.logicbus.backend.AbstractServant;
import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.bizlog.BizLogger;
import com.logicbus.backend.message.JsonMessage;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;
import com.logicbus.models.servant.ServiceDescription;

/**
 * 恢复BizLogger的处理
 * 
 * @author duanyy
 *
 * @since 1.2.7.2
 */
public class BizLoggerResume extends AbstractServant {

	@Override
	protected void onDestroy() {

	}

	@Override
	protected void onCreate(ServiceDescription sd) throws ServantException {

	}

	@Override
	protected int onXml(MessageDoc msgDoc, Context ctx) throws Exception {
		XMLMessage msg = (XMLMessage) msgDoc.asMessage(XMLMessage.class);
		
		Settings settings = Settings.get();
		
		BizLogger bizLogger = (BizLogger) settings.get("bizLogger");
		
		if (bizLogger != null){
			bizLogger.resume();
			
			Element root = msg.getRoot();
			Document doc = root.getOwnerDocument();
			
			Element logger = doc.createElement(bizLogger.getHandlerType());
			bizLogger.report(logger);
			root.appendChild(logger);
		}
		return 0;
	}

	@Override
	protected int onJson(MessageDoc msgDoc, Context ctx) throws Exception {
		JsonMessage msg = (JsonMessage)msgDoc.asMessage(JsonMessage.class);
		
		Settings settings = Settings.get();
		
		BizLogger bizLogger = (BizLogger) settings.get("bizLogger");
		
		if (bizLogger != null){
			bizLogger.resume();
			
			Map<String,Object> root = msg.getRoot();
		
			Map<String,Object> logger = new HashMap<String,Object>();	
			bizLogger.report(logger);
			root.put(bizLogger.getHandlerType(), logger);
		}
		return 0;
	}

}
