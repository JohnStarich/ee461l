package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.controllers.AbstractMongoDBTest;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.DBCollection;
import de.caluga.morphium.annotations.Index;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Test for AbstractModel
 * Created by johnstarich on 4/19/16.
 */
public class AbstractModelTest extends AbstractMongoDBTest {
	public void testSave() throws Exception {
		MovieMatcherDatabase.morphium.getDatabase().dropDatabase(); // cleanup from other test cases
		DBTester obj = new DBTester(new ObjectId(), "test");
		DBCollection collection = MovieMatcherDatabase.morphium.getDatabase().getCollection("d_b_tester");
		assertEquals(0, collection.count());
		obj.save();
		assertEquals(1, collection.count());
	}

	public void testLoad() throws Exception {
		DBTester obj = new DBTester(new ObjectId(), "test");
		assertFalse(obj.load().isPresent());
		obj.save();
		assertTrue(obj.load().isPresent());

		DBTester obj2 = new DBTester(new ObjectId(), "test");
		Optional<DBTester> obj2load = obj2.save().load();
		assertTrue(obj2load.isPresent());
		assertEquals(obj2, obj2load.get());
	}

	public void testExists() throws Exception {
		DBTester obj = new DBTester(new ObjectId(), "test");
		assertFalse(obj.exists());
		obj.save();
		assertTrue(obj.exists());
	}

	public void testDelete() throws Exception {
		DBTester obj = new DBTester(new ObjectId(), "test");
		obj.save();
		assertTrue(obj.exists());
		obj.delete();
		assertFalse(obj.exists());
	}

	public void testSearch() throws Exception {
		MovieMatcherDatabase.morphium.getDatabase().dropDatabase(); // cleanup from other test cases
		DBTester obj = new DBTester(new ObjectId(), "test").save();
		List<DBTester> results = AbstractModel.search(DBTester.class, "test");
		assertEquals(1, results.size());
		assertEquals(obj, results.get(0));
	}
}

@Index({"name:text"})
class DBTester extends AbstractModel<DBTester> {
	public final String name;

	public DBTester(ObjectId id, String name) {
		super(DBTester.class, id);
		this.name = name;
	}

	public boolean equals(Object any) {
		if(any == null || ! (any instanceof DBTester)) return false;
		if(this == any) return true;
		DBTester dbTester = (DBTester)any;
		return id.equals(dbTester.id) && name.equals(dbTester.name);
	}
}
