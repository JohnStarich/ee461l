package com.johnstarich.moviematcher.app;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.runtime.Network;
import junit.framework.TestCase;

/**
 * Created by johnstarich on 4/14/16.
 */
public abstract class AbstractMongoDBTest extends TestCase {
	/**
	 * EmbeddedMongo author's note:
	 * please store Starter or RuntimeConfig in a static final field
	 * if you want to use artifact store caching (or else disable caching)
	 */
	private static final MongodStarter starter;
	private static final int MONGO_PORT = 27017;
	private MongodExecutable mongodExe;
	private MongodProcess mongod;
	private MongoClient mongo;

	static {
		Command command = Command.MongoD;


		ProcessOutput processOutput = new ProcessOutput(Processors.namedConsole("[mongod]"),
			Processors.namedConsole("[MONGOD]"), Processors.namedConsole("[command]"));

		IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
			.defaults(command)
			.artifactStore(new ExtractedArtifactStoreBuilder()
				.defaults(command)
				.download(new DownloadConfigBuilder()
					.defaultsForCommand(command).build())
				.executableNaming(new UserTempNaming()))
			.processOutput(processOutput)
			.build();

		starter = MongodStarter.getInstance(runtimeConfig);
	}

	@Override
	protected void setUp() throws Exception {
		mongodExe = starter.prepare(new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
				.build());
		mongod = mongodExe.start();

		super.setUp();

		mongo = new MongoClient("localhost", MONGO_PORT);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		mongod.stop();
		mongodExe.stop();
	}

	public Mongo getMongo() {
		return mongo;
	}
}