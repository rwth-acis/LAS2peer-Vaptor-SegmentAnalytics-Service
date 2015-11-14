package i5.las2peer.services.analytics;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.QueryParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.annotations.swagger.ApiInfo;
import i5.las2peer.restMapper.annotations.swagger.ApiResponse;
import i5.las2peer.restMapper.annotations.swagger.ApiResponses;
import i5.las2peer.restMapper.annotations.swagger.Summary;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.services.analytics.database.DatabaseManager;
import i5.las2peer.services.analytics.util.OIDC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;

import com.arangodb.entity.GraphEntity;

/**
 * LAS2peer Service
 * 
 * 
 * 
 * 
 */
@Path("analytics")
@Version("0.1")
@ApiInfo(title = "Analytics Service", 
	description = "<p>A RESTful service for Segment analytics for Vaptor.</p>", 
	termsOfServiceUrl = "", 
	contact = "siddiqui@dbis.rwth-aachen.de", 
	license = "MIT", 
	licenseUrl = "") 
	
public class AnalyticsClass extends Service {

	private String port;
	private String username;
	private String password;
	private String database;
	private String databaseServer;
	private String driverName;
	private String hostName;
	private String useUniCode;
	private String charEncoding;
	private String charSet;
	private String collation;
	private String userinfo;
	
	private String userPreferenceService;
	

	private DatabaseManager dbm;
	private String epUrl;
	
	GraphEntity graphNew;
	

	public AnalyticsClass() {
		// read and set properties values
		setFieldValues();

		if (!epUrl.endsWith("/")) {
			epUrl += "/";
		}
		// instantiate a database manager to handle database connection pooling
		// and credentials
		// dbm = new DatabaseManager(username, password, host, port, database);
	}

	@GET
	@Path("weight")
	public HttpResponse getWeight(@QueryParam(name="edge" , defaultValue = "0") int edge){

		dbm = new DatabaseManager();
		dbm.init(driverName, databaseServer, port, database, this.username, password, hostName);
		
		float weight = dbm.getWeight(edge);
		
		HttpResponse r = new HttpResponse(Float.toString(weight));
		r.setStatus(200);
		return r;
	}
	
	@GET
	@Path("domain")
	public HttpResponse getDomain(@QueryParam(name="edge" , defaultValue = "0") int edge){

		dbm = new DatabaseManager();
		dbm.init(driverName, databaseServer, port, database, this.username, password, hostName);
		
		String domain = dbm.getDomain(edge);
		
		HttpResponse r = new HttpResponse(domain);
		r.setStatus(200);
		return r;
	}
	
	
	@GET
	@Path("")
	public HttpResponse getAnalytics(@QueryParam(name="key" , defaultValue = "*") String key, 
			@QueryParam(name="value" , defaultValue = "0") int value){
		
		dbm = new DatabaseManager();
		dbm.init(driverName, databaseServer, port, database, this.username, password, hostName);
		JSONArray analytics=null;
		HttpResponse r = null;
		
		if(key.equals("edge"))
			analytics = dbm.getAnalyticsByEdge(value);
		
		else if(key.equals("user"))
			analytics = dbm.getAnalyticsByUser(value);
		
		else{
			r.setStatus(404);
			return r;
		
		}
		r = new HttpResponse(analytics.toString());
		r.setStatus(200);
		
		return r;
	}
	
	@POST
	@Path("recommendation")
	public HttpResponse postRecommendation(
			@QueryParam(name = "Authorization", defaultValue = "") String access_token,
			@QueryParam(name="edge" , defaultValue = "0") int edgeId,
			@QueryParam(name="weight" , defaultValue = "0") int stars){
		
		String username = null;
		
		System.out.println("TOKEN: "+access_token);
		String token = access_token;
		
		if(token!=null){
			   token = token.replace("Bearer ","");
			   username = OIDC.verifyAccessToken(token, userinfo);
		}
		
		HttpResponse r = null;
		dbm = new DatabaseManager();
		dbm.init(driverName, databaseServer, port, database, this.username, password, hostName);
		
		//JSONObject recommendationObject = new JSONObject(recommendation.toString());
		
		//int weight = dbm.getWeight(recommendationObject.getInt("edgeId"));
		
		float userWeight = dbm.getUserWeight(edgeId, username);
		float segmentWeight = dbm.getWeight(edgeId);
		
		float currentWeight = 0;
		float newSegmentWeight =0;
		//try {
			
		String expLevel = getResponse(userPreferenceService+"/expertise"+"?Authorization=Bearer%20"+token+
				"&domain="+dbm.getDomain(edgeId));
		
		System.out.println("response: "+Float.parseFloat(expLevel.replace("\n", "")));
		currentWeight = stars*Float.parseFloat(expLevel.replace("\n", ""));

		// Save the user's rating in the database
		dbm.updateUserWeight(edgeId, currentWeight, username);
		
		System.out.println("currentWeight: "+currentWeight);
		
		
		// If User has already rated the video segment
		if(userWeight!=0){
			newSegmentWeight = (segmentWeight-userWeight)+((userWeight+currentWeight)/2);
		}
		// It is the first time user is rating the segment
		else{
			newSegmentWeight = segmentWeight+currentWeight;
		}
			
		dbm.updateWeight(edgeId, newSegmentWeight);
			
		/*} catch (JSONException e) {
			r.setStatus(500);
			System.out.println("JSONException: "+e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//preferenceObject.getString("location")
		
		r = new HttpResponse(String.valueOf(newSegmentWeight));
		r.setStatus(200);
		return r;
	}
	
	// Get response from the given uri
	private String getResponse(String uri){
		
		CloseableHttpResponse response = null;
		URI httpRequest;
		String preferenceString = null;
		
		try {
			httpRequest = new URI(uri);
		
			CloseableHttpClient httpPreferenceService = HttpClients.createDefault();
			HttpGet getPreferences = new HttpGet(httpRequest);
			response = httpPreferenceService.execute(getPreferences);
			
	        HttpEntity entity = response.getEntity();
	        
	        if (entity != null) {
	            InputStream instream = entity.getContent();
	            preferenceString = convertStreamToString(instream);
	            //System.out.println("RESPONSE: " + preferenceString);
	            instream.close();
	        }
        
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return preferenceString;
		
	}
	
	private static String convertStreamToString(InputStream is) {
		
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	
	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line + "\n");
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
	
	
	
	
	// ================= Swagger Resource Listing & API Declarations
	// =====================

	@GET
	@Path("api-docs")
	@Summary("retrieve Swagger 1.2 resource listing.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger 1.2 compliant resource listing"),
			@ApiResponse(code = 404, message = "Swagger resource listing not available due to missing annotations."), })
	@Produces(MediaType.APPLICATION_JSON)
	public HttpResponse getSwaggerResourceListing() {
		return RESTMapper.getSwaggerResourceListing(this.getClass());
	}

	@GET
	@Path("api-docs/{tlr}")
	@Produces(MediaType.APPLICATION_JSON)
	@Summary("retrieve Swagger 1.2 API declaration for given top-level resource.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Swagger 1.2 compliant API declaration"),
			@ApiResponse(code = 404, message = "Swagger API declaration not available due to missing annotations."), })
	public HttpResponse getSwaggerApiDeclaration(@PathParam("tlr") String tlr) {
		return RESTMapper.getSwaggerApiDeclaration(this.getClass(), tlr, epUrl);
	}

	/**
	 * Method for debugging purposes. Here the concept of restMapping validation
	 * is shown. It is important to check, if all annotations are correct and
	 * consistent. Otherwise the service will not be accessible by the
	 * WebConnector. Best to do it in the unit tests. To avoid being
	 * overlooked/ignored the method is implemented here and not in the test
	 * section.
	 * 
	 * @return true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid())
			return true;
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is
	 * no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}

}
