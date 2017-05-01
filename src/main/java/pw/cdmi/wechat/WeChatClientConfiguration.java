package pw.cdmi.wechat;

import pw.cdmi.core.http.client.ClientConfiguration;

public class WeChatClientConfiguration extends ClientConfiguration {
	public static final long TOKEN_USEFUL_TIME = 2 * 60 * 60; //微信TOKEN最大存活時間為2小時
}
