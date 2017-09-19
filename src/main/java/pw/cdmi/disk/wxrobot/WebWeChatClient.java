/* 
 * 版权声明(Copyright Notice)：
 *     Copyright(C) 2017-2017 聚数科技成都有限公司。保留所有权利。
 *     Copyright(C) 2017-2017 www.cdmi.pw Inc. All rights reserved. 
 * 
 *     警告：本内容仅限于聚数科技成都有限公司内部传阅，禁止外泄以及用于其他的商业目
 */ 
package pw.cdmi.disk.wxrobot;

/************************************************************
 * @Description:
 * <pre>
 * TODO(对类的简要描述说明 – 必须).<br/>
 * TODO(对类的作用含义说明 – 可选).<br/>
 * TODO(对类的使用方法说明 – 可选).<br/>
 * </pre>
 * @author    伍伟
 * @version   3.0.1
 * @Project   Alpha CDMI Service Platform, wechat-robot Component. 2017年9月13日
 ************************************************************/
public interface WebWeChatClient {
    // 向微信发送消息
    static final String WEBPUSH_URL = "https://api.weixin.qq.com/cgi-bin/poi/getpoilist";

    
    /**
     * 自动接受新的好友请求
     */
    public String AutoAcceptFriends(String message);
}

