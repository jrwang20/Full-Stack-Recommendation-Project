package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;


public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item>	recommendedItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getConnection();
		
		//step1: get all favorited items
		Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
		
		//step2: get all categories of favorited items, sort by count
		Map<String, Integer> allCategories = new HashMap<>();
		for(String itemId : favoriteItemIds) {
			Set<String> categories = conn.getCategories(itemId);
			for(String category : categories) {
				//如果存在这个category，那么增加；否则进行default
				//这里可以直接用getOrDefault方法
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		
		List<Entry<String, Integer>> categoryList = 
				new ArrayList<>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				//这里的Integer.compare()方法是最保险的方法去比较两个int的大小
				return Integer.compare(o2.getValue(), o1.getValue()); //这里是降序，所以o2在前
			}
		});
		
		//step3: do search based on category, filter our favorited events, sort by distance
		Set<Item> visitedItems = new HashSet<>();
		
		for(Entry<String, Integer> category : categoryList) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<>();
			for(Item item: items) {
				if(!favoriteItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			
			Collections.sort(filteredItems, new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					return Double.compare(item1.getDistance(), item2.getDistance()); //这里是升序，所以o1在前
				}
			});
			
			visitedItems.addAll(items); //这个是为了不要重复推荐的dedup的Set
			recommendedItems.addAll(filteredItems);
		}
		return recommendedItems;
	}
}
