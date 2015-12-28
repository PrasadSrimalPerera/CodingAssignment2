package com.ericsson.corp;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import static org.junit.Assert.assertEquals;

/**
 * @author prasad
 * EricssonCorporationEntryService Rest API tests
 */
public class EricssonCorporationEntryServiceTest extends JerseyTest {
	/**
	 * Context parameters (used to configure MongoDB)
	 */
	public static Map<String, String> contextMap;

	static {
		contextMap = new HashMap<String, String>();
		contextMap.put("server", "localhost");
		contextMap.put("port", "27017");
		contextMap.put("database", "test");
		contextMap.put("collection", "entries_test");
	}

	@Before
	public void setUpTest() throws UnknownHostException {
		// Nothing to do here
	}

	/**
	 * Tear down MongoDB test setup
	 * @throws UnknownHostException
	 */
	@After
	public void tearDownTest() throws UnknownHostException {
		MongoClient mongoClient = new MongoClient(contextMap.get("server"), 
				Integer.parseInt(contextMap.get("port")));
		DB db = mongoClient.getDB(contextMap.get("database"));
		DBCollection myCollection = db.getCollection(contextMap.get("collection"));
		myCollection.drop();
		db.dropDatabase();
		mongoClient.close();
	}

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new GrizzlyWebTestContainerFactory();
	}

	/**
	 * Create the test deployment of EricssonCorporationEntryService
	 */
	@Override
	protected DeploymentContext configureDeployment() {
		ResourceConfig config = new ResourceConfig(EricssonCorporationEntryService.class);
		return ServletDeploymentContext.forServlet
				(new ServletContainer(config))
				.contextParams(contextMap)
				.addListener(EricssonCorporationContextListener.class).build();
	}

	/**
	 * Add 2 documents and verify the returned JSon responses.
	 */
	@Test
	public void testAddEntry() {
		String data1 = "Test Document:1";
		String data2 = "Test Document:2";
		
		// Post document 1
		String response1 = target("entryservice/content").request().
				post(Entity.json(data1), String.class);
		JSONObject responseJson = new JSONObject(response1);
		
		assertEquals(responseJson.getInt("entryID"), 1);
		assertEquals(responseJson.getString("content"), data1);
		
		// Post document 2
		String response2 = target("entryservice/content").request().
				post(Entity.json(data2), String.class);
		responseJson = new JSONObject(response2);
		
		assertEquals(responseJson.getInt("entryID"), 2);
		assertEquals(responseJson.getString("content"), data2);	
		
	}

	/**
	 * Post a document and retrieve the document
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 */
	@Test
	public void testGetEntry() throws UnknownHostException {
		String data = "Test Document:1";
		
		// Post document
		String response1 = target("entryservice/content").request().
				post(Entity.json(data), String.class);
		JSONObject responseJson = new JSONObject(response1);
		
		assertEquals(responseJson.getInt("entryID"), 1);
		assertEquals(responseJson.getString("content"), data);

		// Retrieve posted document
		String response2 = target("entryservice/" + responseJson.getInt("entryID"))
				.request().get(String.class);
		responseJson = new JSONObject(response2);
		
		assertEquals(responseJson.getInt("entryID"), 1);
		assertEquals(responseJson.getString("content"), data);
		
	}

	/**
	 * Retrieve non existing document
	 */
	@Test
	public void testGetNotExistEntry() {
		// Retrieve posted document
		Response response = target("entryservice/10")
				.request().get();
		
		assertEquals(response.getStatus(), 204);
	}
	
	/**
	 * Post 3 entries and retrieve and verify them all
	 */
	@Test
	public void testGetEntries() {
		String[] data = {"Test Document:1", "Test Document:2", "Test Document:3" };
		// Post document 1
		target("entryservice/content").request().
				post(Entity.json(data[0]), String.class);
		
		// Post document 2
		target("entryservice/content").request().
				post(Entity.json(data[1]), String.class);
		
		// Post document 3
		target("entryservice/content").request().
				post(Entity.json(data[2]), String.class);
		
		String responseAll = target("entryservice/").request().get(String.class);
		JSONArray responseArray = new JSONArray(responseAll);
		
		// The order of the documents are assumed here.
		for (int i = 0; i < 3; ++i) {
			assertEquals(responseArray.getJSONObject(i).getInt("entryID"), 
					i + 1);
			assertEquals(responseArray.getJSONObject(i).getString("content"), 
					data[i]);
		}
		
	}

	/**
	 * post a document and delete once, delete twice and 
	 * verify the responses
	 */
	@Test
	public void testDeleteEntry() {
		String data = "Test Document:1";
		// Post document
		String response1 = target("entryservice/content").request().
				post(Entity.json(data), String.class);
		JSONObject responseJson = new JSONObject(response1);
		
		assertEquals(responseJson.getInt("entryID"), 1);
		assertEquals(responseJson.getString("content"), data);

		Response responseDelete = target("entryservice/" + responseJson.getInt("entryID"))
				.request().delete();
		assertEquals(responseDelete.getStatus(), 200);
		
		responseDelete = target("entryservice/" + responseJson.getInt("entryID"))
				.request().delete();
		assertEquals(responseDelete.getStatus(), 400);
	}

}
