package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.store.ConfigManager;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Render an HTML file as a String we can send in a response body.
 */
public class ServeStaticFileRoute implements Route {
	private static final String STATIC_PREFIX = "static";
	private static final Map<String, StaticFile> loadedFiles = new ConcurrentHashMap<>(16);
	private static final boolean SHOULD_CACHE_FILES = ConfigManager.getPropertyOrDefault("ENVIRONMENT", "development").equals("production");

	private final String file;

	public ServeStaticFileRoute() {
		this.file = null;
	}

	public ServeStaticFileRoute(String file) {
		this.file = file;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		StaticFile staticFile = getPage(this.file != null ? this.file : request.uri());
		response.type(staticFile.contentType);
		return staticFile.content;
	}

	private static StaticFile getPage(String path) throws HttpException {
		StringBuilder relativeFilePathBuilder = new StringBuilder(STATIC_PREFIX);
		if(! path.startsWith("/")) relativeFilePathBuilder.append('/');
		relativeFilePathBuilder.append(path);
		String relativeFilePath = relativeFilePathBuilder.toString();

		if(SHOULD_CACHE_FILES && loadedFiles.containsKey(relativeFilePath)) {
			return loadedFiles.get(relativeFilePath);
		}

		try {
			URL url = MovieMatcherApplication.class.getClassLoader().getResource(relativeFilePath);
			if (url != null) {
				Path filePath = Paths.get(url.toURI());
				String fileContents = new String(Files.readAllBytes(filePath), Charset.defaultCharset());
				String fileContentType = getContentType(path);
				StaticFile staticFile = new StaticFile(fileContents, fileContentType);
				loadedFiles.put(relativeFilePath, staticFile);
				return staticFile;
			}
			else throw new HttpException(HttpStatus.NOT_FOUND);
		}
		catch(URISyntaxException | IOException e) {
			throw new HttpException(HttpStatus.NOT_FOUND);
		}
	}

	private static String getContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
		switch(extension) {
			case "css": return "text/css";
			case "eot": return "application/vnd.ms-fontobject";
			case "gif": return "image/gif";
			case "html": return "text/html";
			case "ico": return "image/x-icon";
			case "jpg":case "jpeg": return "image/jpeg";
			case "js": return "application/javascript";
			case "png": return "image/png";
			case "svg": return "image/svg+xml";
			case "txt": return "text/plain";
			case "woff": return "application/font-woff";
			case "woff2": return "application/font-woff2";
			case "xml": return "application/xml";
			default: return "application/octet-stream";
		}
	}
}
