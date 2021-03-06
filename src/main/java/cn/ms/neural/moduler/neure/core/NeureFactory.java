package cn.ms.neural.moduler.neure.core;

import java.util.concurrent.TimeoutException;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import cn.ms.neural.common.exception.neure.NeureCallbackException;
import cn.ms.neural.common.exception.neure.NeureException;
import cn.ms.neural.common.exception.neure.NeureNonFaultTolerantException;
import cn.ms.neural.common.exception.neure.NeureTimeoutException;
import cn.ms.neural.common.exception.neure.NeureUnknownException;
import cn.ms.neural.common.spi.Adaptive;
import cn.ms.neural.moduler.Moduler;
import cn.ms.neural.moduler.neure.INeure;
import cn.ms.neural.moduler.neure.Neuron;
import cn.ms.neural.moduler.neure.entity.NeureEntity;
import cn.ms.neural.moduler.neure.processor.INeureProcessor;
import cn.ms.neural.moduler.neure.support.NeureConvert;
import cn.ms.neural.moduler.senior.alarm.AlarmType;

/**
 * 神经元
 * 
 * @author lry
 * @version v1.0
 */
@Adaptive
public class NeureFactory<REQ, RES> implements INeure<REQ, RES> {

	Moduler<REQ, RES> moduler;
	NeureEntity neureEntity;
	
	@Override
	public void notify(Moduler<REQ, RES> moduler) {
		this.moduler=moduler;
		
		//变更通知
		neureEntity=NeureConvert.convert(this.moduler.getUrl());
	}
	
	@Override
	public void init() throws Throwable {
		
	}

	@Override
	public RES neure(REQ req, INeureProcessor<REQ, RES> processor, Object... args) throws NeureException {
		RES res=null;
		Neuron<REQ, RES> neureHandler=null;
		
		try {
			neureHandler=new Neuron<REQ, RES>(req, neureEntity, processor, args);
			res=neureHandler.execute();//执行或容错
		}catch(HystrixBadRequestException nonft){
			throw new NeureNonFaultTolerantException(nonft.getMessage(), nonft);//不执行容错异常
		}catch(HystrixRuntimeException hre){
			if(hre.getCause() instanceof TimeoutException){
				throw new NeureTimeoutException(hre.getMessage(), hre);//超时异常
			}else{
				throw new NeureException(hre.getMessage(), hre);//其他异常
			}
		} catch (Throwable unknown) {
			throw new NeureUnknownException(unknown.getMessage(), unknown);//未知异常
		} finally {
			try {
				processor.callback(res, args);//回调
			} catch (Throwable callback) {
				//$NON-NLS-callback$
				neureHandler.doAlarm("callback", AlarmType.CALLBACK, callback);
				throw new NeureCallbackException(callback.getMessage(), callback);//回调异常
			}
			
//			//指标一
//			List<HystrixEventType> hystrixEventTypes=neureHandler.getExecutionEvents();
//			System.out.println("事件:"+hystrixEventTypes.toString());
//			
//			//指标二
//			HystrixMetrics metrics=neureHandler.getMetrics();
//			System.out.println("指标:"+metrics.toString());
//			
//			//指标三
//			long retryExecuteTimes=neureHandler.getRetryExecuteTimes();
//			System.out.println("重试执行次数:"+retryExecuteTimes);
		}
		
		return res;
	}
	
	@Override
	public void destory() throws Throwable {
		
	}

}
