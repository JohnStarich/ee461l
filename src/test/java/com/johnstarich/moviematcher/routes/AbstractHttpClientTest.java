package com.johnstarich.moviematcher.routes;

import com.johnstarich.moviematcher.utils.HttpException;
import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import spark.Spark;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * API Route Tester
 * Created by johnstarich on 5/28/16.
 */
public class AbstractHttpClientTest extends TestCase {
	private static final Charset charset = Charset.forName("UTF-8");

	private HttpClient client;

	public void testClientWorks() throws Exception {
		Spark.get("/", (request, response) -> "It works!");
		Spark.awaitInitialization();
		get("/", response -> {
			assertEquals("It works!", response.body);
			assertEquals("text/html", response.type);
			assertEquals("UTF-8", response.encoding);
			assertEquals(200, response.status);
		});
	}

	@Override
	public void setUp() throws Exception {
		client = new HttpClient();
		client.start();
	}

	@Override
	public void tearDown() throws Exception {
		client.stop();
	}

	protected void get(String path, ResponseHandler handler) throws HttpException { service(HttpMethod.GET, path, handler); }
	protected void get(String path, Map<String, String> headers, ResponseHandler handler) throws HttpException { service(HttpMethod.GET, path, headers, handler); }

	protected void delete(String path, ResponseHandler handler) throws HttpException { service(HttpMethod.DELETE, path, handler); }
	protected void delete(String path, Map<String, String> headers, ResponseHandler handler) throws HttpException { service(HttpMethod.DELETE, path, headers, handler); }

	protected void post(String path, Object data, ResponseHandler handler) throws HttpException { service(HttpMethod.POST, path, data, handler); }
	protected void post(String path, Object data, Map<String, String> headers, ResponseHandler handler) throws HttpException { service(HttpMethod.POST, path, data, headers, handler); }

	protected void put(String path, Object data, ResponseHandler handler) throws HttpException { service(HttpMethod.PUT, path, data, handler); }
	protected void put(String path, Object data, Map<String, String> headers, ResponseHandler handler) throws HttpException { service(HttpMethod.PUT, path, data, headers, handler); }

	protected void trace(String path, ResponseHandler handler) throws HttpException { service(HttpMethod.TRACE, path, handler); }
	protected void trace(String path, Map<String, String> headers, ResponseHandler handler) throws HttpException { service(HttpMethod.TRACE, path, headers, handler); }

	protected void head(String path, ResponseHandler handler) throws HttpException { service(HttpMethod.HEAD, path, handler); }
	protected void head(String path, Map<String, String> headers, ResponseHandler handler) throws HttpException { service(HttpMethod.HEAD, path, headers, handler); }

	private Request request(HttpMethod method, String path) {
		return client.newRequest("http://localhost:4567")
			.path(path)
			.method(method);
	}

	protected void service(HttpMethod method, String path, ResponseHandler handler) throws HttpException {
		service(method, path, null, null, handler);
	}

	protected void service(HttpMethod method, String path, Object data, ResponseHandler handler) throws HttpException {
		service(method, path, data, null, handler);
	}

	protected void service(HttpMethod method, String path, Map<String, String> headers, ResponseHandler handler) throws HttpException {
		service(method, path, null, headers, handler);
	}

	protected void service(HttpMethod method, String path, Object data, Map<String, String> headers, ResponseHandler handler) throws HttpException {
		final Request request = request(method, path);

		if(headers != null) headers.forEach(request::header);

		if(data != null) {
			String json;
			try {
				json = new JsonTransformer().render(data);
			}
			catch (Exception e) {
				throw new AssertionError("Error serializing data: " + data, e);
			}
			request.content(providerForData(json));
		}

		HttpContentResponse response;
		try {
			response = (HttpContentResponse) request.send();
		}
		catch (Exception e) {
			throw new AssertionError("Error sending request: " + method.asString() + ' ' + path, e);
		}

		HttpResponseWrapper wrappedResponse = new HttpResponseWrapper(response);
		if(wrappedResponse.status / 100 != 2) {
			throw new HttpException(wrappedResponse.status, wrappedResponse.body);
		}

		handler.handle(wrappedResponse);
	}

	private static ContentProvider providerForData(String data) {
		return new ContentProvider.Typed() {
			ByteBuffer buffer = ByteBuffer.wrap(data.getBytes(charset));

			@Override
			public Iterator<ByteBuffer> iterator() {
				return Collections.singletonList(buffer).iterator();
			}

			@Override
			public long getLength() {
				return buffer.limit();
			}

			@Override
			public String getContentType() {
				return "application/json";
			}
		};
	}
}
