package com.logicbus.backend.metrics.core;

import java.io.InputStream;




import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.anysoft.stream.Handler;
import com.anysoft.util.Factory;
import com.anysoft.util.IOTools;
import com.anysoft.util.Properties;
import com.anysoft.util.Settings;
import com.anysoft.util.XmlTools;
import com.anysoft.util.resource.ResourceFactory;

/**
 * 指标接口
 * 
 * @author duanyy
 * @since 1.2.8
 * 
 * @version 1.2.8.1 [20140919 duanyy]
 * - getInstance拆分为getClientInstance和getServerInstance
 */
public interface MetricsHandler extends Handler<Fragment>,MetricsCollector{
	public static class TheFactory extends Factory<MetricsHandler>{
		/**
		 * a logger of log4j
		 */
		protected static final Logger logger = LogManager.getLogger(TheFactory.class);
		
		public static MetricsHandler getClientInstance(Properties p){
			String master = p.GetValue("metrics.handler.client.master", 
					"java:///com/logicbus/backend/metrics/core/metrics.handler.client.xml#com.logicbus.backend.metrics.core.MetricsHandler");
			String secondary = p.GetValue("metrics.handler.client.master", 
					"java:///com/logicbus/backend/metrics/core/metrics.handler.client.xml#com.logicbus.backend.metrics.core.MetricsHandler");
			
			return getInstance(master,secondary,p);
		}
		
		public static MetricsHandler getServerInstance(Properties p){
			String master = p.GetValue("metrics.handler.server.master", 
					"java:///com/logicbus/backend/metrics/core/metrics.handler.server.xml#com.logicbus.backend.metrics.core.MetricsHandler");
			String secondary = p.GetValue("metrics.handler.client.master", 
					"java:///com/logicbus/backend/metrics/core/metrics.handler.server.xml#com.logicbus.backend.metrics.core.MetricsHandler");
			
			return getInstance(master,secondary,p);
		}
		
		/**
		 * 根据环境变量中的配置来创建MetricsHandler
		 */
		protected static MetricsHandler getInstance(String master,String secondary,Properties props){			
			ResourceFactory rf = Settings.getResourceFactory();
			InputStream in = null;
			try {
				in = rf.load(master,secondary, null);
				Document doc = XmlTools.loadFromInputStream(in);		
				if (doc != null){
					return getInstance(doc.getDocumentElement(),props);
				}
			}catch (Throwable ex){
				logger.error("Error occurs when load xml file,source=" + master, ex);
			}finally {
				IOTools.closeStream(in);
			}
			return null;
		}		
		protected static TheFactory instance = new TheFactory();
		
		public static MetricsHandler getInstance(Element e,Properties p){
			return instance.newInstance(e, p);
		}
	}	
}
