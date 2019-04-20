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
				
				for(int i = 0; i < venues.length(); ++i) { //����++i����i++��һ��
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder(); //��ÿһ���ҵ�����Ϣappend������
					
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
						return sb.toString(); //����������ص�һ��String��Ҳ����Array�ڵĵ�һ��address
					}
				}
			}
		}
		
		return "";
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if(!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
			for(int i = 0; i < images.length(); ++i) { //�����images��һ��Container����ô����Ҫһ��method����ȡlength
				JSONObject image = images.getJSONObject(i); //����ֻҪ��һ���͹���
				
				if(!image.isNull("url")) {
					return image.getString("url"); //����ҵ���url��ֱ�ӷ�����
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
				//�������Ӳ��ɹ�����Ĵ���
				//��һ����ʱ��������
			}
			
			//���ڼ�������ɹ��ˣ��Ǿ�Ҫ�ӷ��ص����������ȡ���
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			//�������Щnew BufferedReader, new InputStreamReader�ȵȣ����Ǵ�����obj������һЩhandle
			//���巢�����õĻ�����Ҫȥ��������ӵ�е�method�ſ���
			String inputline;
			StringBuilder response = new StringBuilder();
			while((inputline = in.readLine()) != null) { //readLine����һ�ζ�һ��
				response.append(inputline);
			}
			in.close();
			
			JSONObject obj = new JSONObject(response.toString());
			if(obj.isNull("_embedded")) {//�������ص�obj�ǲ���null
				//�����key��_embedded����TicketMaster���ص�JSON���������һ��id��data��Ҫװ��������Կ�TicketMaster������response�ṹ���ܣ�
				return new ArrayList<>();
			}
			
			JSONObject embedded = obj.getJSONObject("_embedded"); //���ص���һ����JSON��������Ҫ�ҵ�������ŵ�JSON��embedded
			JSONArray events = embedded.getJSONArray("events"); //��embedded�����ְ���һ��JSOBArray������events
			
			//System.out.println(events);
			return getItemList(events);
		}catch(Exception e) {
			e.printStackTrace(); //���ﲢû�д���ֻ�Ǵ�ӡ��һ��
		}
		return new ArrayList<>();
	}
	
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		try {
			for(Item event : events) {
				System.out.println(event.toJSONObject()); //���ﲻ��ֱ��print event����Ϊ���Ǹ�obj��Ӧ��ת��ΪJSON���������ڹ۲�
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
