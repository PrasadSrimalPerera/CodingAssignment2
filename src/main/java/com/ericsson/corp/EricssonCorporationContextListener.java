package com.ericsson.corp;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import javax.servlet.ServletContext;
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
	/**
	 * MongoDB related elements
	 */
	private static MongoClient mongoClient;
	private static DB mongoDB;
	public static DBCollection mongoCollection;
	
	/**
	 * MongoDB configuration parameters
	 */
	private static final String SERVER = "server";
	private static final String SERVER_PORT = "port";
	private static final String DATABASE = "database";
	private static final String COLLECTION = "collection";
	
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// Close MongoDB client
		mongoClient.close();		
	}
	
	/**
	 * Retrieve the last id of the document belongs to the collection
	 * This is to simply to persist documents without id clash when restarting
	 * the application.
	 * @return	document id of the last persisted
	 */
	public static Integer getLastID() {
		Integer id = 0;
		DBCursor objectCursor =  mongoCollection.find().
				sort(new BasicDBObject(EricssonCorporationEntry.ENTRY_DOCUMENT_ID, 
						-1)).
				limit(1);
		
		if (objectCursor.hasNext())
			id = (Integer) objectCursor.next().get(
					EricssonCorporationEntry.ENTRY_DOCUMENT_ID);
		return id;
	}

	/**
	 * Initialize db connections and collections and next id resolution
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			ServletContext servletContext = event.getServletContext();
			mongoClient = new MongoClient(servletContext.getInitParameter(SERVER),
					Integer.parseInt(servletContext.getInitParameter(SERVER_PORT)));
			mongoDB = mongoClient.getDB(servletContext.getInitParameter(DATABASE));
			mongoCollection = mongoDB.getCollection(servletContext.getInitParameter(COLLECTION));

			// Resolve latest id in collection
			EricssonCorporationEntry.initItemIDIncrment(getLastID());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
