package com.logicbus.selector.impl;

import org.w3c.dom.Element;

import com.anysoft.formula.DataProvider;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.selector.FieldList;
import com.logicbus.selector.Selector;

/**
 * 字段组选择器
 * 
 * @author duanyy
 * @since 1.2.8
 * 
 */
public class FieldGroup extends Selector {

	@Override
	public void onConfigure(Element _e, Properties _p) throws BaseException {
		seperator = PropertiesConstants.getString(_p, "seperator", seperator,true);
		noSeperator = PropertiesConstants.getBoolean(_p, "noSeperator", noSeperator,true);
		fieldList = new FieldList();
		fieldList.configure(_e, _p);
	}

	@Override
	public String onSelect(DataProvider _dataProvider) {
		StringBuffer buffer = new StringBuffer();
		int index = 0;
		Selector [] fields = fieldList.getFields();
		for (Selector field : fields) {
			buffer.append(field.select(_dataProvider));
			index++;
			if (!noSeperator && index != fields.length) {
				buffer.append(seperator);
			}
		}
		return buffer.toString();
	}

	protected String seperator = "|";
	
	protected boolean noSeperator = false;
	
	protected FieldList fieldList = null;
	
	public Selector [] getFields(){return fieldList.getFields();}	
}
