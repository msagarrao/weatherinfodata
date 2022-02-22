package com.example.restservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

@RestController
public class KylinController {

	@GetMapping("/weatherinfo")
	public List<WeatherInfo> timelineRequestHttpClient() throws Exception {
		//set up the end point
		String apiEndPoint="https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
		String location="London,UK";
		String startDate=null;
		String endDate=null;
		
		String unitGroup="metric";
		String apiKey="2QZGGGZJ2KWAXKFWCN8MUF4Q3";
	
		StringBuilder requestBuilder=new StringBuilder(apiEndPoint);
		requestBuilder.append(URLEncoder.encode(location, StandardCharsets.UTF_8.toString()));
		
		if (startDate!=null && !startDate.isEmpty()) {
			requestBuilder.append("/").append(startDate);
			if (endDate!=null && !endDate.isEmpty()) {
				requestBuilder.append("/").append(endDate);
			}
		}
		
		URIBuilder builder = new URIBuilder(requestBuilder.toString());
		
		builder.setParameter("unitGroup", unitGroup)
			.setParameter("key", apiKey);

		
		
		HttpGet get = new HttpGet(builder.build());
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		CloseableHttpResponse response = httpclient.execute(get);    
		
		String rawResult=null;
		try {
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				System.out.printf("Bad response status code:%d%n", response.getStatusLine().getStatusCode());
				return null;
			}
			
			HttpEntity entity = response.getEntity();
		    if (entity != null) {
		    	rawResult=EntityUtils.toString(entity, Charset.forName("utf-8"));
		    }
		    
		    
		} finally {
			response.close();
		}
		
		return parseTimelineJson(rawResult);
		
	}
	private List<WeatherInfo> parseTimelineJson(String rawResult) {
		
		if (rawResult==null || rawResult.isEmpty()) {
			System.out.printf("No raw data%n");
			return null;
		}
		
		JSONObject timelineResponse = new JSONObject(rawResult);
		
		ZoneId zoneId=ZoneId.of(timelineResponse.getString("timezone"));
		
		System.out.printf("Weather data for: %s%n", timelineResponse.getString("resolvedAddress"));
		
		JSONArray values=timelineResponse.getJSONArray("days");

		List<WeatherInfo> weatherInfos = new ArrayList<>();
		
		System.out.printf("Date\tMaxTemp\tMinTemp\tPrecip\tSource%n");
		for (int i = 0; i < values.length(); i++) {
			JSONObject dayValue = values.getJSONObject(i);
            
            ZonedDateTime datetime=ZonedDateTime.ofInstant(Instant.ofEpochSecond(dayValue.getLong("datetimeEpoch")), zoneId);
            
            double maxtemp=dayValue.getDouble("tempmax");
            double mintemp=dayValue.getDouble("tempmin");
            double pop=dayValue.getDouble("precip");
            String source=dayValue.getString("source");
			WeatherInfo weatherInfo = new WeatherInfo();
			weatherInfo.setMaxTemp(maxtemp);
			weatherInfo.setMinTemp(mintemp);
			weatherInfo.setPop(pop);
			weatherInfo.setSource(source);
			weatherInfo.setZonedDateTime(datetime);
			weatherInfos.add(weatherInfo);
            System.out.printf("%s\t%.1f\t%.1f\t%.1f\t%s%n", datetime.format(DateTimeFormatter.ISO_LOCAL_DATE), maxtemp, mintemp, pop,source );
        }
		return weatherInfos;
	}







// 	String apiEndPoint="https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
// String location="London, UK";
// String startDate=null; //optional (omit for forecast)
// String endDate=null; //optional (requires a startDate if present)
// String unitGroup="metric"; //us,metric,uk 
// String apiKey="2QZGGGZJ2KWAXKFWCN8MUF4Q3";

// 	private static final String template = "Hello, %s!";
// 	private final AtomicLong counter = new AtomicLong();

// 	@GetMapping("/greeting")
// 	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {

// 		StringBuilder requestBuilder=new StringBuilder(apiEndPoint);
// requestBuilder.append(location);
		
// if (startDate!=null && !startDate.isEmpty()) {
// 	requestBuilder.append("/").append(startDate);
// 	if (endDate!=null && !endDate.isEmpty()) {
// 		requestBuilder.append("/").append(endDate);
// 	}
// }
		
// //Build the parameters to send via GET or POST
// URIBuilder builder = new URIBuilder(requestBuilder.toString());
// builder.setParameter("unitGroup", unitGroup)
// 	.setParameter("key", apiKey);

// //for GET requests, add the parameters to the request
// if ("GET".equals(method)) {
// 	requestBuilder.append("?").append(paramBuilder);
// }

// 		return new Greeting(counter.incrementAndGet(), String.format(template, name));
// 	}
}
