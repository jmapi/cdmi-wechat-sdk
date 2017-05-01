package pw.cdmi.wechat;

public class WXCredentials {
	
    private String appid = null;
    private String secret = null;
    
    public WXCredentials(String appid, String secret){
    	this.appid = appid;
    	this.secret = secret;
    }
    
	public String getAppid() {
		return appid;
	}

	public String getSecret() {
		return secret;
	}

}
