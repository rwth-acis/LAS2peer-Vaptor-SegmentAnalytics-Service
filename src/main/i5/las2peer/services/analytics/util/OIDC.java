package i5.las2peer.services.analytics.util;

import java.net.URI;
import java.net.URISyntaxException;

//import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;


public class OIDC {
	
	public static String verifyAccessToken(String Header, String userinfo) throws JSONException{

		if(Header!=null){
			//String INPUT_FILE = "oidc";
			//String BASE_INPUT_FILE = "base";
	
			Header = Header.replace("Bearer ","");
			//String userinfo, base;
			//base = GetProperty.getParam("uri", BASE_INPUT_FILE);
			//userinfo = base + GetProperty.getParam("userinfo", INPUT_FILE);
			
			
			//userinfo = GetProperty.getParam("userinfo", INPUT_FILE);
			
			BearerAccessToken accessToken = null;
			
			accessToken = new BearerAccessToken(Header);
			
			
			System.out.println("Verify, accessToken:  "+accessToken);
			// *** *** *** Make a UserInfo endpoint request *** *** *** //
	
			// Note: The PayPal IdP uses an older OIDC draft version and
			// is at present not compatible with the Nimbus OIDC SDK so
			// we cannot use its helper call. We can however make a direct
			// call and simply display the raw data.
	
			URI userinfoEndpointURL = null;
			try {
				userinfoEndpointURL = new URI(userinfo);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			// Append the access token to form actual request
			UserInfoRequest userInfoRequest = new UserInfoRequest(userinfoEndpointURL, accessToken);
			
			HTTPResponse httpResponse;
			
			try {
				httpResponse = userInfoRequest.toHTTPRequest().send();
	
			} catch (Exception e) {
	
				// The URL request failed
				System.out.println("Couldn't send HTTP request to UserInfo endpoint: " + e.getMessage());
				//Response.ResponseBuilder r = Response.status(404);
				//return CORS.makeCORS(r, _corsHeaders);
				return "error";
			}
			
			UserInfoResponse userInfoResponse;
			
			try {
				userInfoResponse = UserInfoResponse.parse(httpResponse);
				
			} catch (ParseException e) {
				
				System.out.println("Couldn't parse UserInfo response: " + e.getMessage());
				//Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				//return CORS.makeCORS(r, _corsHeaders);
				return "error";
			}
			
			
			if (userInfoResponse instanceof UserInfoErrorResponse) {
				
				System.out.println("UserInfo request failed");
				//Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				//return CORS.makeCORS(r, _corsHeaders);
				return "error";
			}
			
			
			UserInfo userInfo = ((UserInfoSuccessResponse)userInfoResponse).getUserInfo();
			
	
			//System.out.println("UserInfo: " + userInfo.getProfile().);
			
			String username = null;
			try {
				JSONObject jsonObject = new JSONObject(userInfo.toJSONObject());
				System.out.println("UserInfo: " +jsonObject.toString());
				username = jsonObject.getString("preferred_username");
				/*if(!Java2MySql.approvedUser(username)){
					Response.ResponseBuilder r = Response.status(401);
					return CORS.makeCORS(r, _corsHeaders);
				}*/
				//System.out.println(new PrettyJson().parseAndFormat(userInfo.toJSONObject().toString()));
	
			} catch (Exception e) {
	
				System.out.println("Couldn't parse UserInfo JSON object: " + e.getMessage());
				return "error";
			}
			
			
			//Response.ResponseBuilder r = Response.ok(username);
			//return CORS.makeCORS(r, _corsHeaders);
			return username;
		}
		else{
		   	//Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
	   		//return CORS.makeCORS(r, _corsHeaders);
			return "error";
	   }
	}

}
