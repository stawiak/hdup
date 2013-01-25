package com.os;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.os.actor.read.ReadMasterActor;
import com.os.actor.service.IncomingHandlerActor;
import com.typesafe.config.ConfigFactory;
import org.springframework.stereotype.Service;

/**
 * @author Vadim Bobrov
 */
@Service
public class ActorService {

	private ActorSystem system;
	private ActorRef incomingHandler;
	private ActorRef readMaster;

	public ActorService() {
		system = ActorSystem.create("prod", ConfigFactory.load().getConfig("prod"));
		incomingHandler = system.actorOf(new Props(IncomingHandlerActor.class), "incomingHandler");
		readMaster = system.actorOf(new Props(ReadMasterActor.class), "readMaster");
	}

	public ActorSystem getSystem() {
		return system;
	}

	public ActorRef getIncomingHandler() {
		return incomingHandler;
	}

	public ActorRef getReadMaster() {
		return readMaster;
	}
}
