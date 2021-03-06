package cn.ms.neural.moduler.senior.reliable;

import cn.ms.neural.Adaptor;

/**
 * 高可靠性设计
 * 
 * @author lry
 */
public interface IReliable<REQ, RES> extends Adaptor {

	/**
	 * 业务执行逻辑
	 */
	void execute();
	
	/**
	 * 链路有效性检测
	 * <p>
	 * 心跳机制:<p>
	 * 1.TCP心跳<p>
	 * 2.协议层心跳<p>
	 * 3.应用层心跳<p>
	 * 
	 * 心跳类型：<p>
	 * 1.Ping-Pong型:即请求-响应型<p>
	 * 2.Ping-Ping型:双向心跳<p>
	 * 
	 * 心跳策略:<p>
	 * 1.连续N次心跳都没有收到答应的消息,则心跳超时<p>
	 * 2.读取/发送心跳消息发生I/O异常,则心跳失败
	 */
	boolean check();
	
	/**
	 * 断连重连机制
	 * <p><p>
	 * 当发生如下异常时,客户端需要释放资源后重新发起连接:<p>
	 * 1.服务端因某种原因,主动关闭连接,客户端检测到链路被正常关闭<p>
	 * 2.服务端因宕机等故障,强制关闭连接,客户端检测到链路被Rest掉<p>
	 * 3.心跳检测超时,客户端主动关闭连接<p>
	 * 4.客户端因为其他原因(如:解码失败),强制关闭连接<p>
	 * 5.网络类故障,如网络丢包、超时、单通等,导致链路中断
	 */
	void reconnect();
	
	/**
	 * 消息缓存重复
	 * <p>
	 * 设计思路:<p>
	 * 1.失败后立即缓存,等待重试(推荐)<p>
	 * 2.发起前缓存,成功则清除,失败则等待重试
	 */
	void mcacherc();
	
	/**
	 * 资源优雅释放
	 * <p>
	 * 设计思路:使用注册JDK的ShutdownHook实现优雅停机
	 */
	void release();
	
	/**
	 * 重复握手保护
	 * <p>
	 * 当客户端握手成功之后,在链路处于正常状态下,不允许客户端
	 * 重复握手,防止客户端在异常状态下反复重连导致句柄资源被耗尽
	 */
	void rhandshake();
	
}
