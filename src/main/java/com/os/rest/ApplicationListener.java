package com.os.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Vadim Bobrov
 */
public class ApplicationListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		//ActorSystem system = ActorSystem.create("prod", ConfigFactory.load().getConfig("prod"));
		//ActorRef incomingHandler = system.actorOf(new Props(IncomingHandlerActor.class), "incomingHandler");
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
