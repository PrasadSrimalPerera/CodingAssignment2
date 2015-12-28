package com.ericsson.corp;

import java.util.List;
import java.util.ArrayList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteConcernException;
import com.mongodb.WriteResult;

/**
 * 
 * @author prasad EricssonCorporationEntry presents a data entry type from
 * application to MongoDB Document
 */
public class EricssonCorporationEntry {
	private int entryID; // Entry ID
	private String content; // Content relevant to the entry

	/**
	 * Collection document entry keys
	 */
	public static String ENTRY_DOCUMENT_ID = "_id";
	public static String ENTRY_DOCUMENT_CONTENT = "content";

	/**
	 * Retrieve entry ID
	 * 
	 * @return entry ID
	 */
	public int getEntryID() {
		return entryID;
	}

	/**
	 * Retrieve content
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Constructor EricssonCorporationEntry
	 * @param entryID	entry ID
	 * @param content	content
	 */
	public EricssonCorporationEntry(int entryID, String content) {
		this.entryID = entryID;
		this.content = content;
	}

	/**
	 * Item ID increment represents the entry id, incremented with each entry
	 * adding. At the initiation, this Id is initialized to the last document Id
	 * in the MongoDB collection
	 */
	private static int itemIDIncrement = -1;

	/**
	 * Retrieve next id for the document entry
	 * @return next id
	 */
	private static synchronized int getNextID() {
		return ++itemIDIncrement;
	}

	/**
	 * Initialize item increment Id
	 * @param id	latest id in collection
	 */
	public static synchronized void initItemIDIncrment(int id) {
		itemIDIncrement = id;
	}

	/**
	 * Add document entry to MongoDB collection
	 * @param content	content
	 * @return EricssonCorporationEntry if successful
	 */
	public static EricssonCorporationEntry addItem(String content) {
		// create the next entry
		BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();
		int entryID = getNextID();
		docBuilder.append(ENTRY_DOCUMENT_ID, entryID);
		docBuilder.add(ENTRY_DOCUMENT_CONTENT, content);
		DBObject nextEntry = docBuilder.get();

		try {
			// insert the entry
			EricssonCorporationContextListener.mongoCollection.
			insert(nextEntry, WriteConcern.UNACKNOWLEDGED);
			
			return new EricssonCorporationEntry(entryID, content);
		} catch (WriteConcernException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Retrieve collection item
	 * @param id	Id of the item to be retrieved
	 * @return	item if found in the collection
	 */
	public static EricssonCorporationEntry getItem(int id) {
		DBObject query = BasicDBObjectBuilder.start().add(ENTRY_DOCUMENT_ID, id).get();
		DBCursor cursor = EricssonCorporationContextListener.mongoCollection.find(query);
		if (cursor.hasNext()) {
			DBObject item = cursor.next();
			String content = (String) item.get(ENTRY_DOCUMENT_CONTENT);
			int itemId = (Integer) item.get(ENTRY_DOCUMENT_ID);
			return new EricssonCorporationEntry(itemId, content);
		}
		return null;
	}

	/**
	 * Retrieve all the items in the collection
	 * @return List of EricssonCorporationEntry (items)
	 */
	public static List<EricssonCorporationEntry> getAllItems() {
		List<EricssonCorporationEntry> items = new ArrayList<>();
		DBCursor cursor = EricssonCorporationContextListener.mongoCollection.find();
		while (cursor.hasNext()) {
			DBObject item = cursor.next();
			String content = (String) item.get(ENTRY_DOCUMENT_CONTENT);
			int itemId = (Integer) item.get(ENTRY_DOCUMENT_ID);
			items.add(new EricssonCorporationEntry(itemId, content));
		}
		return items;
	}

	/**
	 * Remove a document from collection
	 * @param id	id of the document to remove
	 * @return	true if delete successful
	 */
	public static boolean deleteItem(int id) {
		try {
			DBObject query = BasicDBObjectBuilder.start().
					add(ENTRY_DOCUMENT_ID, id).get();
			WriteResult writeResult = EricssonCorporationContextListener.
					mongoCollection.remove(query);
			
			if (writeResult.getN() == 0)	
				return false;	// No elements effected, deleting non existing document.
			else
				return true;
		} catch (WriteConcernException e) {
			e.printStackTrace();
			return false;
		}
	}
}
