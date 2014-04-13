package com.logicbus.examples;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.logicbus.backend.Context;
import com.logicbus.backend.Servant;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.backend.message.XMLMessage;

public class Helloworld6 extends Servant {

	@Override
	public int actionProcess(MessageDoc msgDoc, Context ctx) throws Exception{
		XMLMessage msg = (XMLMessage)msgDoc.asMessage(XMLMessage.class);		
		Element root = msg.getRoot();
		Document doc = root.getOwnerDocument();
		
		
		Element hello = doc.createElement("Hello");
		
		hello.appendChild(doc.createTextNode("Hello world"));
		root.appendChild(hello);
		
		return 0;
	}

}
