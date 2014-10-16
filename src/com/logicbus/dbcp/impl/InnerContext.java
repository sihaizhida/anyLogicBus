package com.logicbus.dbcp.impl;

import com.anysoft.context.Context;
import com.anysoft.context.Inner;
import com.logicbus.dbcp.core.ConnectionPool;

/**
 * Source配置文件内联的Context实现
 * 
 * @author duanyy
 *
 * @since 1.2.9
 */
public class InnerContext extends Inner<Context<ConnectionPool>>{

	@Override
	public String getObjectName() {
		return "dbcp";
	}

	@Override
	public String getDefaultClass() {
		return XMLConfigurableImpl.class.getName();
	}

}
