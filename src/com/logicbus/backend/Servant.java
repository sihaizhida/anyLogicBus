package com.logicbus.backend;

import com.anysoft.util.PropertiesConstants;
import com.logicbus.backend.message.*;
import com.logicbus.models.servant.ServiceDescription;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * 服务员(所有服务实现的基类)
 * 
 * @author duanyy
 *
 */
abstract public class Servant {
	/**
	 * 服务描述
	 */
	protected ServiceDescription m_desc = null;
	
	/**
	 * 服务员的工作状态
	 */
	private int m_state;
	
	/**
	 * 服务调用超时时间
	 */
	private long m_time_out = 3000;
	
	/**
	 * 工作状态:繁忙
	 */
	public static final int STATE_BUSY = 0;
	
	/**
	 * 工作状态：空闲
	 */
	public static final int STATE_IDLE = 1;
	
	/**
	 * 获取服务者的工作状态
	 * @return state
	 */
	public int getState(){return m_state;}
	
	/**
	 * 设置服务者的工作状态
	 * @param state 工作状态
	 */
	public void setState(int state){m_state = state;}
	
	/**
	 * 构造函数
	 */
	public Servant(){
		
	}
	
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = null;
	
	/**
	 * 初始化服务者
	 * 
	 * <br>
	 * 根据服务描述初始化服务者,在{@link com.logicbus.backend.ServantPool ServantPool}类
	 * {@link com.logicbus.backend.ServantPool#CreateServant(ServiceDescription) CreateServant}时调用。
	 * 
	 * @param sd service description
	 * @throws ServantException 
	 */
	public void create(ServiceDescription sd) throws ServantException{
		m_desc = sd;
		
		if (logger == null) {
			logger = LogManager.getLogger(Servant.class.getName());
		}

		m_time_out = PropertiesConstants.getLong(sd.getProperties(), "time_out", 3000);
	}

	/**
	 * 获取超时时长
	 * @return value
	 */
	public long getTimeOutValue(){return m_time_out;}
	
	/**
	 * 判断是否已经超时
	 * @param start_time start time
	 * @return if time out return true,otherwise false.
	 */
	public boolean isTimeOut(long start_time){
		long current = System.currentTimeMillis();
		if (current - start_time > m_time_out) return true;
		return false;
	}
	
	/**
	 * 销毁服务
	 * 
	 * <br>
	 * 在{@link com.logicbus.backend.ServantPool ServantPool}类
	 * {@link com.logicbus.backend.ServantPool#close() close}时调用。
	 * 
	 */
	public void destroy(){
		
	}

	/**
	 * 获取服务描述
	 * @return 服务描述
	 */
	public ServiceDescription getDescription(){return m_desc;}
	
	/**
	 * 服务处理过程
	 * @param msg 消息文档
	 * @param ctx 上下文
	 * @return 
	 * @throws Exception
	 */
	abstract public int actionProcess(MessageDoc msg,Context ctx) throws Exception;
	
	/**
	 * 服务处理即将开始
	 * 
	 * <br>调度框架在{@link #actionProcess(MessageDoc, Context)}之前调用.
	 * @param doc 消息文档
	 * @param ctx 上下文
	 * @see #actionProcess(MessageDoc, Context)
	 */
	public void actionBefore(MessageDoc doc,Context ctx){
		logger.debug("Begin:" + m_desc.getName());
	}
	
	/**
	 * 服务有效性测试
	 * @param msg 消息文档
	 * @param ctx 上下文
	 * @return 
	 */
	public int actionTesting(MessageDoc msg,Context ctx){
		logger.debug("Testing the service,I am ok!!!");	
		return 0;
	}
	
	/**
	 * 服务处理已经结束
	 * 
	 * <br>调度框架在{@link #actionProcess(MessageDoc, Context)}之后调用.
	 * 
	 * @param doc 消息文档
	 * @param ctx 上下文
	 * @see #actionProcess(MessageDoc, Context)
	 */
	public void actionAfter(MessageDoc doc,Context ctx){
		ctx.setReturnCode("core.ok");
		ctx.setReason("It is successful");
		ctx.setEndTime(System.currentTimeMillis());
		logger.debug("Successful:" + m_desc.getName());
		logger.debug("Duration(ms):" + (ctx.getEndTime() - ctx.getStartTime()));
	}
	
	/**
	 * 服务处理发生异常
	 * 
	 * <br>调度框架在{@link #actionProcess(MessageDoc, Context)}抛出异常时调用
	 * 
	 * @param doc 消息文档
	 * @param ctx 上下文
	 * @see #actionProcess(MessageDoc, Context)
	 * @param ex 
	 */
	public void actionException(MessageDoc doc,Context ctx, ServantException ex){	
		ctx.setReturnCode( ex.getCode());
		ctx.setReason(ex.getMessage());
		
		ctx.setEndTime(System.currentTimeMillis());
		logger.debug("Failed:" + m_desc.getName());
		logger.debug("Duration(ms):" + (ctx.getEndTime() - ctx.getStartTime()));		
	}
}
