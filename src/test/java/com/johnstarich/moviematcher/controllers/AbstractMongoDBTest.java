package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.routes.AbstractHttpClientTest;
import com.johnstarich.moviematcher.store.ConfigManager;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
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

import java.util.stream.StreamSupport;

/**
 * MongoDB enabled TestCase
 * Created by johnstarich on 4/14/16.
 */
public abstract class AbstractMongoDBTest extends AbstractHttpClientTest {
	/**
	 * EmbeddedMongo author's note:
	 * please store Starter or RuntimeConfig in a static final field
	 * if you want to use artifact store caching (or else disable caching)
	 */
	private static final MongodStarter starter;
	private static final int MONGO_PORT = 27017;
	private static MongodExecutable mongodExe;
	private static MongodProcess mongod;
	private static MongoClient mongo;

	static {
		// if MONGO_TEST environment variable is set, then prevent multiple firewall popup warnings
		if(ConfigManager.getPropertyOrDefault("MONGO_TEST", null) != null) {
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
		else starter = MongodStarter.getDefaultInstance();

		try {
			setUpAll();
		}
		catch(Exception e) {
			throw new RuntimeException("Error initializing embedded MongoDB instance. " +
				"Is another instance running already? Did the last instance not shut down properly?", e);
		}
	}

	protected static void setUpAll() throws Exception {
		mongodExe = starter.prepare(new MongodConfigBuilder()
			.version(Version.Main.PRODUCTION)
			.net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
			.build());
		mongod = mongodExe.start();

		mongo = new MongoClient("localhost", MONGO_PORT);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		StreamSupport.stream(mongo.listDatabaseNames().spliterator(), true)
			.map(mongo::getDatabase)
			.forEach(db ->
				StreamSupport.stream(db.listCollectionNames().spliterator(), true)
					.map(db::getCollection)
					.forEach(MongoCollection::drop)
			);
	}

	protected void tearDownAll() {
		mongod.stop();
		mongodExe.stop();
	}

	public Mongo getMongo() {
		return mongo;
	}
}