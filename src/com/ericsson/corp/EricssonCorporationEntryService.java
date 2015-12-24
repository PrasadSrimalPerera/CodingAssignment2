package com.ericsson.corp;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.google.gson.Gson;

/**
 * EricssonCorporationEntryService implements the Rest APIs to add/retrieve entry items
 * to and from an underlying MongoDB collection repository.
 * @author prasad
 *
 */
@Path ("/entryservice")
public class EricssonCorporationEntryService {
	private static Gson gson = new Gson();	
	
	@Path("/content")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addEntry(String content) {
		EricssonCorporationEntry newEntry = EricssonCorporationEntry.addItem(content);
		if (newEntry != null)
			return Response.ok(gson.toJson(newEntry)).build();
		else
			return Response.status(Status.BAD_REQUEST).build();
	}

	@Path("{s}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntry(@PathParam("s") int id) {
		EricssonCorporationEntry entry = EricssonCorporationEntry.getItem(id);
		if (entry != null)
			return Response.ok(gson.toJson(entry)).build();
		else
			return Response.status(Status.NO_CONTENT).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEntries() {
		List<EricssonCorporationEntry> entries = EricssonCorporationEntry.getAllItems();
		return Response.ok(gson.toJson(entries)).build();
	}
	
	@Path("{c}")
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteEntry(@PathParam("c") int id) {
		if (EricssonCorporationEntry.deleteItem(id))
			return Response.ok().build();
		else
			return Response.status(Status.BAD_REQUEST).build();
	}
}
