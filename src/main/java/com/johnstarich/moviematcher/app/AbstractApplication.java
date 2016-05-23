package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.controllers.AbstractService;
import com.johnstarich.moviematcher.controllers.HttpService;
import spark.servlet.SparkApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple base application that manages currently mounted services' set up and tear down procedures
 * Created by johnstarich on 5/22/16.
 */
public abstract class AbstractApplication implements SparkApplication {
	private List<HttpService> controllers = new ArrayList<>();

	/**
	 * Adds a controller to be mounted when the application starts.
	 * This allows the application to use "services" within the application
	 * without writing all route definitions in one long file.
	 * @param controller The controller to initialize at application start and destroy upon termination.
	 */
	protected void mount(HttpService controller) {
		if(controller instanceof AbstractService) {
			AbstractService abstractServiceController = (AbstractService) controller;
			if (abstractServiceController.PREFIX == null) throw new ExceptionInInitializerError("Prefix for controller mount must not be null");
			System.out.println("Mounting " + abstractServiceController.getClass().getSimpleName() + " to " + abstractServiceController.PREFIX);
		}
		else {
			String mountPoint = controller.mountPoint();
			String resource = controller.resource();
			if(mountPoint == null) throw new ExceptionInInitializerError("Mount point for controller must not be null");
			if(resource == null) throw new ExceptionInInitializerError("Resource name for controller must not be null");
			System.out.println("Mounting " + controller.getClass().getSimpleName() + " to " + controller.mountPoint() + " " + controller.resource());
		}
		controllers.add(controller);
	}

	@Override
	public final void init() {
		this.app();
		AbstractService.initStaticResourceHandlers();
		AbstractService.initCompressionHandlers();
		AbstractService.initErrorHandlers();
		controllers.forEach(HttpService::init);
	}

	@Override
	public final void destroy() {
		controllers.forEach(HttpService::destroy);
	}

	/** Mounts all controllers for this application */
	public abstract void app();
}
