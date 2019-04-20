package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "JKeo4MzANE1p3jVLcnJMPEbktwQSGAHa";
	
	private String getAddress(JSONObject event) throws JSONException {
		if(!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if(!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				
				for(int i = 0; i < venues.length(); ++i) { //这里++i还是i++都一样
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder(); //把每一个找到的信息append到这里
					
					if(!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						
						if(!address.isNull("line1")) {
							sb.append(address.getString("line1"));
						}
						if(!address.isNull("line2")) {
							sb.append(" ");
							sb.append(address.getString("line2"));
						}
						if(!address.isNull("line3")) {
							sb.append(" ");
							sb.append(address.getString("line3"));
						}
					}
					
					if(!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						
						if(!city.isNull("name")) {
							sb.append(" ");
							sb.append(city.getString("name"));
						}
					}
					
					if(!sb.toString().equals("")) {
						return sb.toString(); //这里仅仅返回第一个String，也就是Array内的第一个address
					}
				}
			}
		}
		
		return "";
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if(!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
			for(int i = 0; i < images.length(); ++i) { //这里的images是一个Container，那么就需要一个method来获取length
				JSONObject image = images.getJSONObject(i); //这里只要第一个就够了
				
				if(!image.isNull("url")) {
					return image.getString("url"); //如果找到了url，直接返回了
				}
			}
		}
		
		return "";
	}
	
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		//System.out.println(event.getJSONArray("classifications"));
		if(!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			
			for(int i = 0; i < classifications.length(); ++i) {
				JSONObject classification = classifications.getJSONObject(i);
				
				if(!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					
					if(!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		
		return categories;
	}
	
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		
		for(int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			
			if(!event.isNull("name")) {
				builder.setName(event.getString("name")); 
			}
			
			if(!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			
			if(!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			
			if(!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			if(!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}
		return itemList;
	}
	
	public List<Item> search(double lat, double lon, String keyword) {
		if(keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius", API_KEY, geoHash, keyword, 50);
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			int responseCode = connection.getResponseCode();
			
			System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);
			System.out.println("Response code: " + responseCode);
			
			if(responseCode != 200) {
				//对于连接不成功情况的处理
				//这一次暂时不考虑了
			}
			
			//现在假设请求成功了，那就要从返回的内容里面读取结果
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			//上面的这些new BufferedReader, new InputStreamReader等等，都是创建的obj，都是一些handle
			//具体发挥作用的话，是要去调用它们拥有的method才可以
			String inputline;
			StringBuilder response = new StringBuilder();
			while((inputline = in.readLine()) != null) { //readLine就是一次读一行
				response.append(inputline);
			}
			in.close();
			
			JSONObject obj = new JSONObject(response.toString());
			if(obj.isNull("_embedded")) {//看看返回的obj是不是null
				//这里的key是_embedded，是TicketMaster返回的JSON数据里面的一个id，data主要装在这里（可以看TicketMaster官网的response结构介绍）
				return new ArrayList<>();
			}
			
			JSONObject embedded = obj.getJSONObject("_embedded"); //返回的是一个大JSON，现在需要找到里面包着的JSON，embedded
			JSONArray events = embedded.getJSONArray("events"); //而embedded里面又包着一个JSOBArray，就是events
			
			//System.out.println(events);
			return getItemList(events);
		}catch(Exception e) {
			e.printStackTrace(); //这里并没有处理，只是打印了一下
		}
		return new ArrayList<>();
	}
	
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		try {
			for(Item event : events) {
				System.out.println(event.toJSONObject()); //这里不能直接print event，因为这是个obj，应该转化为JSON，更有利于观察
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//for test
	public static void main(String[] args) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		
		tmAPI.queryAPI(29.682684, -95.295410);
	}
}
