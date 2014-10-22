package com.logicbus.remote.core;

import java.util.Map;

import com.anysoft.util.JsonSerializer;

/**
 * Result的虚基类
 * @author sony
 * 
 * @since 1.2.9.3
 * 
 */
abstract public class AbstractResult implements Result{
	@Override
	public <data extends JsonSerializer> data getData(String id,
			Class<data> clazz) {
		data result = null;
		
		Object found = getData(id);
		if (found != null){
			if (found instanceof Map){
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String,Object>) found;
				try {
					result = clazz.newInstance();
					result.fromJson(map);
				} catch (Exception e) {
					// error occurs
				}
			}
		}
		return result;
	}

	@Override
	public <data extends JsonSerializer> data getData(String id, data result) {
		Object found = getData(id);
		if (found != null){
			if (found instanceof Map){
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String,Object>) found;
				result.fromJson(map);
			}
		}
		return result;
	}

	@Override
	public <data> data getData(String id, Builder<data> builder) {
		data result = null;
		Object found = getData(id);
		if (found != null){
			if (builder != null){
				result = builder.deserialize(id, found);
			}
		}
		return result;
	}

	abstract public Object getData(String id);
}
