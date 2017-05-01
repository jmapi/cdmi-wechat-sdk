/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package pw.cdmi.wechat;

import java.util.concurrent.ConcurrentHashMap;

import pw.cdmi.core.http.auth.Credentials;
import pw.cdmi.core.http.auth.CredentialsProvider;
import pw.cdmi.core.http.auth.DefaultCredentials;
import pw.cdmi.core.http.auth.InvalidCredentialsException;
import pw.cdmi.core.http.auth.SecurityToken;

/**
 * 微信的访问身份提供 implementation of {@link CredentialsProvider}. 
 */
public class WeChatCredentialProvider implements CredentialsProvider {
	//访问的appld以及token信息缓存起来
	private final static ConcurrentHashMap<String, Credentials> cache_credentials = new ConcurrentHashMap<String, Credentials>();
	
    private volatile Credentials creds;
    
    public WeChatCredentialProvider(Credentials creds) {
        setCredentials(creds);
    }
    
    public WeChatCredentialProvider(String accessKeyId, String secretAccessKey) {
        this(accessKeyId, secretAccessKey, null);
    }
    
    public WeChatCredentialProvider(String accessKeyId, String secretAccessKey, String securityToken) {
        checkCredentials(accessKeyId, secretAccessKey);
        Credentials credentials =  cache_credentials.get(accessKeyId);
        SecurityToken token = null;
        if(credentials != null){//凭证存在，检查是否过期
        	//检查cache中是否存在该appId对应的token信息
        	token = credentials.getSecurityToken();
        	if(token == null){
        		token = WeChatClient.getSecurityToken(accessKeyId, secretAccessKey);
        		credentials.setSecurityToken(token);
        	}else{
        		long now = System.currentTimeMillis();
        		boolean isExpired = false;
        		if(token.getExpiretime() != 0){ //token自带失效时间
        			isExpired = (now - token.getCreatetime()) >= token.getExpiretime();
        		}else{//token没有失效时间，就和Token失效规则时间比较
        			isExpired = (now - token.getCreatetime()) >= WeChatClientConfiguration.TOKEN_USEFUL_TIME;
        		}
        		if(isExpired){
        			token = WeChatClient.getSecurityToken(accessKeyId, secretAccessKey);
        			credentials.setSecurityToken(token);
        		}
        	};
        }else{//第一次生成访问凭证
        	credentials = new DefaultCredentials(accessKeyId, secretAccessKey);
        	token = WeChatClient.getSecurityToken(accessKeyId, secretAccessKey);
        	credentials.setSecurityToken(token);
            cache_credentials.put(accessKeyId, credentials);
        }
        setCredentials(credentials);
    }
    
    @Override
    public synchronized void setCredentials(Credentials creds) {
        if (creds == null) {
            throw new InvalidCredentialsException("creds should not be null.");
        }
        
        checkCredentials(creds.getAccessKeyId(), creds.getSecretAccessKey());
        this.creds = creds;
    }

    @Override
    public Credentials getCredentials() {
        if (this.creds == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        return this.creds;
    }
    
    private static void checkCredentials(String accessKeyId, String secretAccessKey) {
        if (accessKeyId == null || accessKeyId.equals("")) {
            throw new InvalidCredentialsException("Access key id should not be null or empty.");
        }
       
        if (secretAccessKey == null || secretAccessKey.equals("")) {
            throw new InvalidCredentialsException("Secret access key should not be null or empty.");
        }
    }

}
