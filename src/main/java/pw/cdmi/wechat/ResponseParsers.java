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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.google.gson.stream.JsonReader;

import pw.cdmi.core.http.auth.SecurityToken;
import pw.cdmi.core.http.client.ResponseMessage;
import pw.cdmi.core.http.client.parser.ResponseParseException;
import pw.cdmi.core.http.client.parser.ResponseParser;

/*
 * A collection of parsers that parse HTTP reponses into corresponding human-readable results.
 */
public final class ResponseParsers {

	public static final TokenResponseParser tokenResponseParser = new TokenResponseParser();
	
	public static final class EmptyResponseParser implements ResponseParser<ResponseMessage> {

		@Override
		public ResponseMessage parse(ResponseMessage response) throws ResponseParseException {
			// Close response and return it directly without parsing.
			try {
				response.close();
			} catch (IOException e) {
			}
			return response;
		}

	}
	
	public static final class TokenResponseParser implements ResponseParser<SecurityToken> {

		@Override
		public SecurityToken parse(ResponseMessage response) throws ResponseParseException {
			InputStream in = response.getContent();
			JsonReader reader;
			try {
				reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
				SecurityToken token = new SecurityToken();
				try {
					reader.beginObject();
					while (reader.hasNext()) {
						String name = reader.nextName();
						if ("access_token".equals(name)) {
							token.setToken(reader.nextString());
						} else if ("expires_in".equals(name)) {
							token.setExpiretime(reader.nextLong());
						} else {
							reader.skipValue();
						}
					}
					reader.endObject();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
				token.setCreatetime(System.currentTimeMillis());
				return token;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
			return null;
		}
	}
}
