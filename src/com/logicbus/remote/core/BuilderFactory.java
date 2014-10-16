package com.logicbus.remote.core;

import java.util.List;
import java.util.Map;

/**
 * 序列化工具工厂
 * 
 * @author duanyy
 *
 * @since 1.2.9
 */
public class BuilderFactory {
	/**
	 * 基于Double类的序列化
	 */
	public static final Builder<Double> DOUBLE = new Builder<Double>(){

		@Override
		public Object serialize(String id, Double o) {
			return o;
		}

		@Override
		public Double deserialize(String id, Object json) {
			if (json instanceof Number){
				return ((Number)json).doubleValue();
			}
			
			if (json instanceof String){
				try {
					return Double.parseDouble((String)json);
				}catch (Exception ex){
					
				}
			}
			return null;
		}
	};
	
	/**
	 * 基于Float类的序列化
	 */
	public static final Builder<Float> FLOAT = new Builder<Float>(){

		@Override
		public Object serialize(String id, Float o) {
			return o;
		}

		@Override
		public Float deserialize(String id, Object json) {
			if (json instanceof Number){
				return ((Number)json).floatValue();
			}
			
			if (json instanceof String){
				try {
					return Float.parseFloat((String)json);
				}catch (Exception ex){
					
				}
			}			
			return null;
		}
	};	
	
	/**
	 * 基于Long类的序列化
	 */
	public static final Builder<Long> LONG = new Builder<Long>(){

		@Override
		public Object serialize(String id, Long o) {
			return o;
		}

		@Override
		public Long deserialize(String id, Object json) {
			if (json instanceof Number){
				return ((Number)json).longValue();
			}
			
			if (json instanceof String){
				try {
					return Long.parseLong((String)json);
				}catch (Exception ex){
					
				}
			}			
			return null;
		}
	};	
	
	/**
	 * 基于Integer类的序列化
	 */
	public static final Builder<Integer> INTEGER = new Builder<Integer>(){

		@Override
		public Object serialize(String id, Integer o) {
			return o;
		}

		@Override
		public Integer deserialize(String id, Object json) {
			if (json instanceof Number){
				return ((Number)json).intValue();
			}
			
			if (json instanceof String){
				try {
					return Integer.parseInt((String)json);
				}catch (Exception ex){
					
				}
			}			
			return null;
		}
	};	
	
	/**
	 * 基于Boolean类的序列化
	 */
	public static final Builder<Boolean> BOOLEAN = new Builder<Boolean>(){

		@Override
		public Object serialize(String id, Boolean o) {
			return o;
		}

		@Override
		public Boolean deserialize(String id, Object json) {
			if (json instanceof Number){
				return ((Number)json).longValue() == 1;
			}
			
			if (json instanceof String){
				try {
					return Boolean.parseBoolean((String)json);
				}catch (Exception ex){
					
				}
			}			
			return null;
		}
	};	
	
	/**
	 * 基于String类的序列化
	 */
	public static final Builder<String> STRING = new Builder<String>(){

		@Override
		public Object serialize(String id, String o) {
			return o;
		}

		@Override
		public String deserialize(String id, Object json) {
			if (json instanceof String){
				return (String)json;
			}
			
			return json.toString();
		}
	};
	
	/**
	 * 基于List<Object>类的序列化
	 */
	public static final Builder<List<Object>> LIST = new Builder<List<Object>>(){

		@Override
		public Object serialize(String id, List<Object> o) {
			return o;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Object> deserialize(String id, Object json) {
			if (json instanceof List){
				return (List<Object>)json;
			}
			
			return null;
		}
	};		
	
	/**
	 * 基于Map<String,Object>类的序列化
	 */
	public static final Builder<Map<String,Object>> MAP = new Builder<Map<String,Object>>(){

		@Override
		public Object serialize(String id, Map<String,Object> o) {
			return o;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<String,Object> deserialize(String id, Object json) {
			if (json instanceof Map){
				return (Map<String,Object>)json;
			}
			
			return null;
		}
	};		
}
