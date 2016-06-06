package com.johnstarich.moviematcher.routes;

import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;
import com.johnstarich.moviematcher.app.MovieMatcherApplication;
import com.johnstarich.moviematcher.store.ConfigManager;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
	private static final boolean SHOULD_CACHE_FILES = ConfigManager.getPropertyOrDefault("CACHE_STATIC_FILES", "false").equals("true") ||
		ConfigManager.getPropertyOrDefault("ENVIRONMENT", "development").equals("production");

	private final String defaultFile;

	public ServeStaticFileRoute() {
		this.defaultFile = null;
	}

	public ServeStaticFileRoute(String defaultFile) {
		this.defaultFile = defaultFile;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		StaticFile staticFile;
		try {
			staticFile = getPage(request.uri());
		}
		catch (HttpException e) {
			if(e.getStatusCode() != HttpStatus.NOT_FOUND.code) throw e;
			staticFile = getPage(this.defaultFile);
		}
		response.type(staticFile.contentType);

		if(staticFile.contentType.equals("text/html")) return staticFile.content;
		else {
			response.body(staticFile.content);
			return null;
		}
	}

	private static String pathForRelativePath(String path) {
		StringBuilder relativeFilePathBuilder = new StringBuilder(STATIC_PREFIX);
		if(! path.startsWith("/")) relativeFilePathBuilder.append('/');
		relativeFilePathBuilder.append(path);
		return relativeFilePathBuilder.toString();
	}

	/** For testing purposes only. Sets the content of a page for a given path. */
	static void setPage(String path, String content, String contentType) throws HttpException {
		loadedFiles.put(pathForRelativePath(path), new StaticFile(content, contentType));
	}

	private static StaticFile getPage(String path) throws HttpException {
		String relativeFilePath = pathForRelativePath(path);

		if(SHOULD_CACHE_FILES && loadedFiles.containsKey(relativeFilePath)) {
			return loadedFiles.get(relativeFilePath);
		}

		try {
			URL url = MovieMatcherApplication.class.getClassLoader().getResource(relativeFilePath);
			if (url != null) {
				Path filePath = Paths.get(url.toURI());
				String fileContents = new String(Files.readAllBytes(filePath));
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
		String extension;
		if(fileName == null || fileName.indexOf('.') < 0) extension = "";
		else extension = fileName.substring(fileName.lastIndexOf('.') + 1);
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
			case "ttf": return "application/x-font-ttf";
			case "txt": return "text/plain";
			case "woff": return "application/font-woff";
			case "woff2": return "application/font-woff2";
			case "xml": return "application/xml";
			default: return "application/octet-stream";
		}
	}
}

class StaticFile {
	public final String content;
	public final String contentType;

	public StaticFile(String content, String contentType) {
		this.content = content;
		this.contentType = contentType;
	}
}
