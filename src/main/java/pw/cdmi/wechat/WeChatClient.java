package pw.cdmi.wechat;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import pw.cdmi.core.Constants;
import pw.cdmi.core.http.HttpMethod;
import pw.cdmi.core.http.auth.CredentialsProvider;
import pw.cdmi.core.http.auth.SecurityToken;
import pw.cdmi.core.http.client.ClientConfiguration;
import pw.cdmi.core.http.client.ClientException;
import pw.cdmi.core.http.client.DefaultServiceClient;
import pw.cdmi.core.http.client.ExecutionContext;
import pw.cdmi.core.http.client.NoRetryStrategy;
import pw.cdmi.core.http.client.RequestChecksumHanlder;
import pw.cdmi.core.http.client.RequestHandler;
import pw.cdmi.core.http.client.RequestMessage;
import pw.cdmi.core.http.client.RequestProgressHanlder;
import pw.cdmi.core.http.client.ResponseChecksumHandler;
import pw.cdmi.core.http.client.ResponseHandler;
import pw.cdmi.core.http.client.ResponseMessage;
import pw.cdmi.core.http.client.ResponseProgressHandler;
import pw.cdmi.core.http.client.RetryStrategy;
import pw.cdmi.core.http.client.ServiceClient;
import pw.cdmi.core.http.client.ServiceException;
import pw.cdmi.core.http.client.TimeoutServiceClient;
import pw.cdmi.core.http.client.model.WebServiceRequest;
import pw.cdmi.core.http.client.parser.ResponseParseException;
import pw.cdmi.wechat.ResponseParsers.EmptyResponseParser;

public class WeChatClient {

	protected CredentialsProvider credsProvider;
	protected ServiceClient client;
	protected static EmptyResponseParser emptyResponseParser = new EmptyResponseParser();
	protected static ErrorResponseHandler errorResponseHandler = new ErrorResponseHandler();
	protected static RetryStrategy noRetryStrategy = new NoRetryStrategy();

	public WeChatClient(String accessKey, String secretAccessKey) {
		this(new WeChatCredentialProvider(accessKey, secretAccessKey), null);
	}

	public WeChatClient(CredentialsProvider credsProvider, ClientConfiguration config) {

		this.credsProvider = credsProvider;
		config = config == null ? new WeChatClientConfiguration() : config;
		if (config.isRequestTimeoutEnabled()) {
			this.client = new TimeoutServiceClient(config);
		} else {
			this.client = new DefaultServiceClient(config);
		}
	}

	public CredentialsProvider getCredentialsProvider() {
		return this.credsProvider;
	}

	/**
	 * Send HTTP request with specified context to OSS and wait for HTTP
	 * response.
	 */
	public ResponseMessage sendRequest(RequestMessage request) throws ServiceException, ClientException {
		return sendRequest(request, false, null, null);
	}

	public ResponseMessage sendRequest(RequestMessage request, boolean keepResponseOpen) throws ServiceException, ClientException {
		return sendRequest(request, keepResponseOpen, null, null);
	}
	
	private ResponseMessage sendRequest(RequestMessage request, boolean keepResponseOpen,
			List<RequestHandler> requestHandlers, List<ResponseHandler> reponseHandlers)
			throws ServiceException, ClientException {

		final WebServiceRequest originalRequest = request.getOriginalRequest();
		request.getHeaders().putAll(client.getClientConfiguration().getDefaultHeaders());
		request.getHeaders().putAll(originalRequest.getHeaders());
		request.getParameters().putAll(originalRequest.getParameters());

		ExecutionContext context = new ExecutionContext();
		context.setCharset(Constants.DEFAULT_CHARSET_NAME);
		context.addResponseHandler(errorResponseHandler);
		if (HttpMethod.POST == request.getMethod()) {
			context.setRetryStrategy(noRetryStrategy);
		}
		context.setCredentials(credsProvider.getCredentials());

		context.addRequestHandler(new RequestProgressHanlder());
		if (requestHandlers != null) {
			for (RequestHandler handler : requestHandlers)
				context.addRequestHandler(handler);
		}
		if (client.getClientConfiguration().isCrcCheckEnabled()) {
			context.addRequestHandler(new RequestChecksumHanlder());
		}

		context.addResponseHandler(new ResponseProgressHandler(originalRequest));
		if (reponseHandlers != null) {
			for (ResponseHandler handler : reponseHandlers)
				context.addResponseHandler(handler);
		}
		if (client.getClientConfiguration().isCrcCheckEnabled()) {
			context.addResponseHandler(new ResponseChecksumHandler());
		}

		return send(request, context, keepResponseOpen);
	}

	protected ResponseMessage send(RequestMessage request, ExecutionContext context, boolean keepResponseOpen)
			throws ServiceException, ClientException {
		ResponseMessage response = null;
		try {
			SecurityToken token = context.getCredentials().getSecurityToken();
			if (token != null && token.getToken() != null) {
				request.addParameter("access_token", token.getToken());
			}
			response = client.sendRequest(request, context);
			return response;
		} catch (ServiceException e) {
			assert (e instanceof ServiceException);
			throw (ServiceException) e;
		} finally {
			if (response != null && !keepResponseOpen) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static SecurityToken getSecurityToken(String accessKey, String secretAccessKey) {
	
		//不走WeChatClient通道，因为走WeChatClient是需要获得token的
		ClientConfiguration config = new WeChatClientConfiguration();
		 
		ServiceClient client = new DefaultServiceClient(config);
		RequestMessage request = new RequestMessage();
		request.setEndpoint(URI.create("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
				+ accessKey + "&secret=" + secretAccessKey));
		request.setMethod(HttpMethod.GET);
		ExecutionContext context = new ExecutionContext();
		ResponseMessage response = client.sendRequest(request, context);
		try {
			return ResponseParsers.tokenResponseParser.parse(response);
		} catch (ResponseParseException e) {
			throw new ClientException(e.getMessage()); 
		}finally{
			client.shutdown();
		}

	}
}
