package com.logicbus.service;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.message.JSONMessage;
import com.logicbus.backend.message.MessageDoc;

/**
 * HelloJSON
 * 
 * <br>
 * 一个小小的测试服务，采用JSON格式向客户端输出了hello.定义在/com/logicbus/service/servant.xml中，具体配置如下:<br>
 * 
 * {@code 
 * <service id="HelloJSON"
 * name="HelloJSON"
 * note="HelloJSON ,我的第一个Logicbus服务。"
 * visible="public"
 * module="com.logicbus.service.HelloJSON"
 * />
 * }
 * 
 * <br>
 * 如果配置在服务器中，访问地址为：<br>
 * {@code
 * http://[host]:[port]/[webcontext]/services/demo/logicbus/HelloJSON 
 * }
 * 
 * @author duanyy
 *
 */
public class HelloJSON extends Servant {
	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception {
		JSONMessage msg = msgDoc.asJSON();
		StringBuffer buf = msg.getBuffer();
		buf.setLength(0);
		buf.append("{\"say\":\"hello world!\"}");
		return 0;
	}
}
