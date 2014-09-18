package com.logicbus.selector.impl;

import org.w3c.dom.Element;

import com.anysoft.formula.DataProvider;
import com.anysoft.formula.DefaultFunctionHelper;
import com.anysoft.formula.Expression;
import com.anysoft.formula.Parser;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;
import com.logicbus.selector.Selector;

/**
 * 公式选择器
 * 
 * @author duanyy
 * @since 1.2.8
 * 
 */
public class Formula extends Selector {

	@Override
	public void onConfigure(Element _e, Properties _p) throws BaseException {
		formula = PropertiesConstants.getString(_p, "formula", formula,true);
		Parser parser = new Parser(new DefaultFunctionHelper(null));
		expr = parser.parse(formula);
	}

	@Override
	public String onSelect(DataProvider _dataProvider) {
		try{
			return expr.getValue(_dataProvider).toString();
		}catch (Exception ex){
			return getDefaultValue();
		}
	}

	protected String formula = "0";
	protected Expression expr = null;	
}
