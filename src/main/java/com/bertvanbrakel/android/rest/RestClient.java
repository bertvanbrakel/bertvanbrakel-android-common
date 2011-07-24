package com.bertvanbrakel.android.rest;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.bertvanbrakel.android.lang.Logger;


/**
 * Modified from the original at
 * http://lukencode.com/2010/04/27/calling-web-services
 * -in-android-using-httpclient/
 */
public class RestClient {

    private static final Logger LOG = new Logger(RestClient.class);

	public static enum RequestMethod {
		POST, GET;
	}

	private final List<NameValuePair> params = new ArrayList<NameValuePair>();

	private final List<NameValuePair> headers = new ArrayList<NameValuePair>();
	private final String baseUrl;

	private int responseCode;
	private String message;
	private String response;

	private HttpResponse httpResponse;

	public String getResponse() {
		return response;
	}

	public String getErrorMessage() {
		return message;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public RestClient(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void addParam(final String name, final String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	public void addParam(final String name, final int value) {
		params.add(new BasicNameValuePair(name, Integer.toString(value)));
	}

	public void addParam(final String name, final boolean value) {
		params.add(new BasicNameValuePair(name, Boolean.toString(value)));
	}

	public void addParam(final String name, final byte[] bytes) {
		params.add(new InputStreamPair(name, bytes));
	}

	public void addHeader(final String name, final String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	public void addHeaders(final Collection<? extends NameValuePair> headers) {
	    this.headers.addAll(headers);
    }

	public void addHeaders(final Map<String, String> nameValues) {
        for(final String name:nameValues.keySet()){
            headers.add(new BasicNameValuePair(name, nameValues.get(name)));
        }
    }


	public Value getHeaderValue(final String name){
		final Header h = httpResponse.getFirstHeader(name);
		return new Value( name, h==null?null:h.getValue() );
	}

	public HttpResponse execute(final RequestMethod method) throws RestClientException {
		switch (method) {
		case GET: {
			// add parameters
			final StringBuilder queryString = new StringBuilder();
			if (!params.isEmpty()) {
				queryString.append("?");
				for (final NameValuePair p : params) {
					if( !(p instanceof InputStreamPair)){
						if (queryString.length() > 1) {
							queryString.append("&");
						}
						queryString.append(p.getName());
						queryString.append('=');
						try {
							queryString.append(URLEncoder.encode(p.getValue(),
									"UTF-8"));
						} catch (final UnsupportedEncodingException e) {
							// should never be throw as we should always be able to
							// encode to UTF-8
							throw new RestClientException(
									"Unexpected query param encoding issue", e);
						}
					}
				}
			}
			final HttpGet get = new HttpGet(baseUrl + queryString.toString());
			//get.getParams().setParameter(ClientPNames.COOKIE_POLICY,CookiePolicy.RFC_2965);

			// add headers
			for (final NameValuePair h : headers) {
				get.addHeader(h.getName(), h.getValue());
			}
			return executeRequest(get, baseUrl);
		}
		case POST: {
			final HttpPost post = new HttpPost(baseUrl);
			//post.getParams().setParameter(ClientPNames.COOKIE_POLICY,CookiePolicy.RFC_2965);

			// add headers
			for (final NameValuePair h : headers) {
				post.addHeader(h.getName(), h.getValue());
			}
			// set the post params
			if (!params.isEmpty()) {
				boolean hasBinary = false;
				//see if we have any binary data. if we do have to handle this differently
				for (final NameValuePair p : params) {
					if( p instanceof InputStreamPair){
						hasBinary = true;
						break;
					}
				}
				if( hasBinary ){
					final MultipartEntity multiPart = new MultipartEntity();
					for (final NameValuePair p : params) {
						if( p instanceof InputStreamPair){
							final InputStreamPair data = (InputStreamPair)p;
							multiPart.addPart(p.getName(),new InputStreamBody(data.getInputStream(), p.getName()));
						} else {
							try {
                                multiPart.addPart(p.getName(),new StringBody(p.getValue()));
                            } catch (final UnsupportedEncodingException e) {
                                throw new RestClientException(String.format("Could not encode param '%s' for http POST", p.getName() ),e);
                            }
						}
					}
					post.setEntity(multiPart);
				} else {
					try {
						post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					} catch (final UnsupportedEncodingException e) {
						// should never be throw as we should always be able to
						// encode to UTF-8
						throw new RestClientException(
								"Unexpected post param encoding issue", e);
					}
				}
			}
			return executeRequest(post, baseUrl);
		}
		default:{
			throw new RestClientException( "Unknown request type " + method );
		}
		}
	}

	private HttpResponse executeRequest(final HttpUriRequest request, final String url) throws RestClientException {
		final HttpClient client = new DefaultHttpClient();
		try {
		    if( LOG.isDebugEnabled()){
		        LOG.debug(String.format("Making HTTP %s request to '%s'", request.getMethod(), url));
		    }
			try {
				httpResponse = client.execute(request);
	            if( LOG.isDebugEnabled()){
	                LOG.debug(String.format("Request completed with http status %s",httpResponse.getStatusLine().getStatusCode()));
	            }
			} catch (final Exception e) {
				throw new RestClientException("Error sending request to server", e);
			}

			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();

			final HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {
				InputStream is = null;
				try {
					is = entity.getContent();
					response = IOUtils.toString(is);
					// Closing the input stream will trigger connection release
				} catch (final Exception e) {
					throw new RestClientException("Error reading response from server",e);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		    if( HttpStatus.GATEWAY_TIMEOUT.equalsCode(responseCode)){
                throw new RestClientException("Timed out contatcing server. Http Status" + HttpStatus.GATEWAY_TIMEOUT );
	        }
			return httpResponse;
		}  finally {
			if (client != null) {
				try {
					client.getConnectionManager().shutdown();
				} catch (final Exception e) {
				    LOG.warn("Error shutting down rest client",e);
					//throw new RestClientException("Error shutting down rest client",e);
				}
			}
		}
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public static class Value {
		private final String value;
		private final String name;

		public Value(final String name,final String value){
			this.name = name;
			this.value = value;
		}

		public String asString() {
			return value;
		}

		public int asInt(){
			return value==null?0:Integer.parseInt(value);
		}
		public long asLong(){
			return value==null?0:Long.parseLong(value);
		}

		public boolean asBoolean() {
			if (value != null) {
				final String b = value.toLowerCase();
				if ("t".equals(b) || "true".equals(b) || "yes".equals(b)
						|| "1".equals(b)) {
					return true;
				}
			}
			return false;
		}

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

	}
}
