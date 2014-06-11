package com.logicbus.models.servant.getter;

import com.anysoft.util.Properties;
import com.logicbus.backend.Context;
import com.logicbus.backend.ServantException;
import com.logicbus.backend.message.Message;
import com.logicbus.backend.message.MessageDoc;
import com.logicbus.models.servant.Argument;
import com.logicbus.models.servant.Getter;

/**
 * 单参数的getter
 * <br>
 * 获取指定参数ID的参数值.参数ID在argument的parameter中配置，变量名为id
 * 
 * @author duanyy
 * @since 1.2.0
 */
public class Single implements Getter{
	protected String field = null;
	public Single(Properties props){
		if (props != null)
			field = props.GetValue("id","");
	}
	@Override
	public String getValue(Argument argu, MessageDoc msg, Context ctx) throws ServantException {
		String id = field == null || field.length() <= 0 ? argu.getId() : field;
		String value;
		if (argu.isOption()){
			value = ctx.GetValue(id, argu.getDefaultValue());
		}else{
			value = ctx.GetValue(id, "");
			if (value == null || value.length() <= 0){
				throw new ServantException("client.args_not_found",
						"Can not find parameter:" + id);
			}
		}
		return value;
	}

	@Override
	public String getValue(Argument argu, Message msg, Context ctx)
			throws ServantException {
		String id = field == null || field.length() <= 0 ? argu.getId() : field;
		String value;
		if (argu.isOption()){
			value = ctx.GetValue(id, argu.getDefaultValue());
		}else{
			value = ctx.GetValue(id, "");
			if (value == null || value.length() <= 0){
				throw new ServantException("client.args_not_found",
						"Can not find parameter:" + id);
			}
		}
		return value;
	}
}
