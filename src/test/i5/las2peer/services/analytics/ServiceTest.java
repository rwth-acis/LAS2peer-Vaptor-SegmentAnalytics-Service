package i5.las2peer.services.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.analytics.AnalyticsClass;
import i5.las2peer.services.analytics.database.DatabaseManager;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Example Test Class demonstrating a basic JUnit test structure.
 * 
 * 
 *
 */
public class ServiceTest {
	
	private static final String HTTP_ADDRESS = "http://127.0.0.1";
    private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;
	
	private static UserAgent testAgent;
	private static final String testPass = "adamspass";
	
	private static final String testServiceClass = "i5.las2peer.services.userPreference.PreferenceClass";
	
	private static final String mainPath = "preference/";
	
	//private static final String objectCollection = "Videos";
	//private static final String objectCollectionSecond = "Images";
	//private static final String annotationCollection = "TextTypeAnnotation";
	//private static final String annotationCollectionSecond = "LocationTypeAnnotation";
	
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {
		
		//start node
		node = LocalNode.newNode();
		node.storeAgent(MockAgentFactory.getAdam());
		node.launch();
		
		ServiceAgent testService = ServiceAgent.generateNewAgent(testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");
		
		node.registerReceiver(testService);
		
		//start connector
		logStream = new ByteArrayOutputStream ();
		
		connector = new WebConnector(true,HTTP_PORT,false,1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream (logStream));
		connector.start ( node );
        Thread.sleep(1000); //wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();
		
        connector.updateServiceList();
        //avoid timing errors: wait for the repository manager to get all services before continuing
        try
        {
            System.out.println("waiting..");
            Thread.sleep(10000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
		
	}
	
	
	/**
	 * Called after the tests have finished.
	 * Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer () throws Exception {
		
		connector.stop();
		node.shutDown();
		
        connector = null;
        node = null;
        
        LocalNode.reset();
		
		System.out.println("Connector-Log:");
		System.out.println("--------------");
		
		System.out.println(logStream.toString());
		
    }
	
	
	/**
	 * Test the ServiceClass for valid rest mapping.
	 * Important for development.
	 */
	@Test
	public void testDebugMapping()
	{
		AnalyticsClass cl = new AnalyticsClass();
		assertTrue(cl.debugMapping());
	}
	
	
	/**
	 * Tests the User Preference Service for getting the preferences for a user
	 */
	//@Ignore("Used only for performance tests")
	@Test
	public void testGetUserPreference()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		JSONObject o;
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
			//for (int i = 0; i < 100; i++){
				//add a new objectCollection
	            ClientResponse result=c.sendRequest("GET", mainPath +"?username=aarij", "*/*", new Pair[]{});
	            assertEquals(200, result.getHttpCode());
	            //assertTrue(result.getResponse().trim().contains("id")); 
				System.out.println("Result of 'testCreateNodes': " + result.getResponse());
			//}
			
			
			/*for (int i = 0; i < 100; i++){
				//add a new objectCollectionSecond
	            ClientResponse result=c.sendRequest("POST", mainPath +"objects", "{\"collection\": \"" + objectCollectionSecond + "\"}", "application/json", "* /*", new Pair[]{});
	            /*assertEquals(200, result.getHttpCode());
	            assertTrue(result.getResponse().trim().contains("id")); 
				System.out.println("Result of 'testCreateNodes': " + result.getResponse());
			}*/
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
	}
	
	
	/**
	 * Tests the User Preference Service for posting the preferences for a user
	 */
	//@Ignore("Used only for performance tests")
	/*@Test
	public void testPostUserPreference()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		JSONObject preferenceObject = new JSONObject();
		
		preferenceObject.put("username","aTestUser");
		preferenceObject.put("location","Hamburg, Germany");
		preferenceObject.put("language","La");
		preferenceObject.put("duration","3");
		
		preferenceObject.put("exp1","co");
		preferenceObject.put("exp2","he");
		preferenceObject.put("lvl1","1");
		preferenceObject.put("lvl2","2");
		preferenceObject.put("noOfExp","2");
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);

			
            ClientResponse result=c.sendRequest("POST", mainPath, preferenceObject.toString(), new Pair[]{});
            assertEquals(200, result.getHttpCode());
            //assertTrue(result.getResponse().trim().contains("id")); 
			System.out.println("Result of 'testCreateNodes': " + result.getResponse());
			
			// Delete the entry
			
			DatabaseManager dbm = new DatabaseManager();
			dbm.init("com.mysql.jdbc.Driver", "mysql", "3306", "userprofile", "annot", "23pnote477", "eiche");
			
			dbm.deletePrefrences("aTestUser");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
	}*/
	
	
	
}
