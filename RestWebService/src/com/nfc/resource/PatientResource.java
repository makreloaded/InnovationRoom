package com.nfc.resource;


import java.sql.SQLException;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Request;

import org.codehaus.jettison.json.JSONException;

import com.nfcrest.model.*;
 
@Path("/patient")
public class PatientResource {
 
     
    
    @Context
    UriInfo uriInfo;
 
    @Context
    Request request;
     
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String respondAsReady() {
        return " NFC Project webservice!!";
    }
 
    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public String doget_data(@QueryParam("id") int pid) throws SQLException, JSONException {
         
    	
    	String res=null;
    	
    	//{
    		 res = DBconnection.get_data(pid);
    		 if(res== null)
    		 {
    			 res = "Patient ID not found";
    		 }
    			
    	
       return  res;
    }
   
}