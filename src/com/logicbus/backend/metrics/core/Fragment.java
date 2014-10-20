package com.logicbus.backend.metrics.core;

import java.util.Map;

import org.w3c.dom.Element;

import com.anysoft.stream.Flowable;
import com.anysoft.util.JsonSerializer;
import com.anysoft.util.JsonTools;
import com.anysoft.util.XmlSerializer;

/**
 * 指标片段
 * 
 * @author duanyy
 * @since 1.2.8
 */
public class Fragment implements XmlSerializer,JsonSerializer,Flowable{

	@Override
	public void toJson(Map<String,Object> json) {
		if (json != null){
			if (dims != null){
				dims.toJson(json);
			}
			if (measures != null){
				measures.toJson(json);
			}
			
			json.put("t", timestamp);
			
			json.put("id", id);
		}
	}

	@Override
	public void fromJson(Map<String,Object> json) {
		if (json != null){
			dims = newDimensions();
			dims.fromJson(json);
			
			measures = newMeasures();
			measures.fromJson(json);
			
			id = JsonTools.getString(json, "id", "");
			
			timestamp = JsonTools.getLong(json, "t", 0);
		}
	}

	@Override
	public void toXML(Element e) {
		if (e != null){
			if (dims != null){
				dims.toXML(e);
			}
			if (measures != null){
				measures.toXML(e);
			}
			
			e.setAttribute("t", String.valueOf(timestamp));
			
			e.setAttribute("id", id);
		}
	}

	@Override
	public void fromXML(Element e) {
		if (e != null){
			dims = newDimensions();
			dims.fromXML(e);
			
			measures = newMeasures();
			measures.fromXML(e);
			
			id = e.getAttribute("id");
			
			{
				String t = e.getAttribute("t");
				if (t != null && t.length() > 0){
					try {
						timestamp = Long.parseLong(t);
					}catch (Exception ex){
						
					}
				}
			}
		}
	}
	
	protected Dimensions newDimensions(){
		return new SimpleDimensions();
	}
	
	protected Measures newMeasures(){
		return new SimpleMeasures();
	}

	public Fragment(){
		id = "";
	}
	
	public Fragment(String _id){
		id = _id;
	}
	
	public Fragment(String _id,Dimensions _dims,Measures _measures){
		id = _id;
		dims = _dims;
		measures = _measures;
	}
	
	@Override
	public String toString(){
		return id + "%" + dims.toString() + "%" + measures.toString() + "%" + timestamp;
	}
	
	/**
	 * 时间戳
	 */
	protected long timestamp = System.currentTimeMillis();
	
	public long getTimestamp(){
		return timestamp;
	}
	
	/**
	 * ID
	 */
	protected String id;
	
	public String getId(){
		return id;
	}

	/**
	 * 维度集
	 */
	protected Dimensions dims;
	
	public Dimensions getDimensions(){
		if (dims == null){
			dims = newDimensions();
		}
		return dims;
	}
	
	/**
	 * 量度集
	 */
	protected Measures measures;
	
	public Measures getMeasures(){
		if (measures == null){
			measures = newMeasures();
		}
		return measures;
	}

	public void incr(Fragment other){
		if (measures != null){
			measures.incr(other.measures);
		}
	}

	@Override
	public String getValue(String varName, Object context, String defaultValue) {
		if (varName.equals("count") || varName.equals("cnt")){
			return dims != null ? String.valueOf(dims.count()) : defaultValue;
		}
		
		if (varName.equals("id")){
			return getId();
		}
		
		if (varName.startsWith("m")){
			String sIdx = varName.substring(1);
			try {
				int idx = Integer.parseInt(sIdx);
				if (measures != null){
					String value = measures.get(idx).toString();
					return value == null ? defaultValue : value;
				}else{
					return defaultValue;
				}
			}catch (Exception ex){
				return defaultValue;
			}
		}
		
		if (varName.startsWith("d")){
			String sIdx = varName.substring(1);
			try {
				int idx = Integer.parseInt(sIdx);
				if (dims != null){
					String value = dims.get(idx);
					return value == null ? defaultValue : value;
				}else{
					return defaultValue;
				}
			}catch (Exception ex){
				return defaultValue;
			}			
		}
		return null;
	}

	@Override
	public Object getContext(String varName) {
		return this;
	}

	@Override
	public String getStatsDimesion() {
		return (dims != null) ? (id + ":" + dims.toString()) : id;
	}
}
