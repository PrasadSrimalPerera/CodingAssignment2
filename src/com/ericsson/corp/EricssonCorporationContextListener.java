package com.ericsson.corp;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 
 * @author prasad
 * EricssonCorporationContextListener implements ServletContextListener
 * which initialize the MongoDb client, DB connection and the collection at the service start up 
 * and close the client and tear down of the service
 */
public class EricssonCorporationContextListener implements ServletContextListener{
	private static MongoClient mongoClient;
	private static DB mongoDB;
	public static DBCollection mongoCollection;
	
	/**
	 * MongoDB configuration parameters
	 */
	public static final String SERVER = "localhost";
	public static final int SERVER_PORT = 27017;
	public static final String DATABASE = "test";
	public static final String COLLECTION = "entries";
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		mongoClient.close();		
	}
	
	/**
	 * Retrieve the last id of the document belongs to the collection
	 * @return	document id of the last persisted
	 */
	public static Integer getLastID() {
		Integer id = 0;
		DBCursor objectCursor =  mongoCollection.find().
				sort(new BasicDBObject("_id", -1)).
				limit(1);
		
		if (objectCursor.hasNext())
			id = (Integer) objectCursor.next().get("_id");
		return id;
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		try {
			mongoClient = new MongoClient(SERVER, SERVER_PORT);
			mongoDB = mongoClient.getDB(DATABASE);
		    mongoCollection = mongoDB.getCollection(COLLECTION);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
