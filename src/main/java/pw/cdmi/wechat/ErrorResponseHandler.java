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

import java.io.IOException;

import org.apache.http.HttpStatus;

import pw.cdmi.core.http.client.ClientException;
import pw.cdmi.core.http.client.ErrorResult;
import pw.cdmi.core.http.client.ResponseHandler;
import pw.cdmi.core.http.client.ResponseMessage;
import pw.cdmi.core.http.client.ServiceException;
import pw.cdmi.core.http.client.parser.JAXBResponseParser;
import pw.cdmi.core.http.client.parser.ResponseParseException;
import pw.cdmi.core.lang.ResourceUtils;

/**
 * 根据返回信息中包含的错误信息，自动转换为异常输出。
 */
public class ErrorResponseHandler implements ResponseHandler {

	public void handle(ResponseMessage response) throws ServiceException, ClientException {

		if (response.isSuccessful()) {
			return;
		}

		String requestId = response.getRequestId();
		int statusCode = response.getStatusCode();
		if (response.getContent() == null) {
			/**
			 * When HTTP response body is null, handle status code 404 Not
			 * Found, 304 Not Modified, 412 Precondition Failed especially.
			 */
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				throw new ClientException();
			} else if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
				throw new ClientException();
			} else if (statusCode == HttpStatus.SC_PRECONDITION_FAILED) {
				throw new ClientException();
			} else {
				throw new ServiceException();
			}
		}

		JAXBResponseParser parser = new JAXBResponseParser(ErrorResult.class);
		try {
			ErrorResult errorResult = (ErrorResult) parser.parse(response);
			throw new WeChatException(errorResult.Message, errorResult.Code, errorResult.RequestId, errorResult.HostId,
					errorResult.Header, errorResult.ResourceURL, errorResult.Method,
					response.getErrorResponseAsString());
		} catch (ResponseParseException e) {
			throw new WeChatException(
					ResourceUtils.COMMON_RESOURCE_MANAGER.getFormattedString("FailedToParseResponse", e.getMessage()),
					null, requestId, null, null, null, response.getErrorResponseAsString());
		} finally {
			try {
				response.close();
			} catch (IOException e) {
			}
		}
	}

}
