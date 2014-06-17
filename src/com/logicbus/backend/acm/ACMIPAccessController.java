package com.logicbus.backend.acm;

import com.logicbus.backend.Context;
import com.logicbus.models.catalog.Path;
import com.logicbus.models.servant.ServiceDescription;


/**
 * 基于IP的ACM访问控制
 * 
 * @author duanyy
 * @since 1.2.3
 */
public class ACMIPAccessController extends ACMAccessController {

	@Override
	public String createSessionId(Path id, ServiceDescription sd,
			Context ctx) {
		return ctx.getClientIp();
	}
}
