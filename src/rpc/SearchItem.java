package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String userId = request.getParameter("user_id");
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String keyword = request.getParameter("term");
		//String keyword = "basketball";
		//String keyword = "baseball";
		
//		TicketMasterAPI tmAPI = new TicketMasterAPI();
//		List<Item> items = tmAPI.search(lat, lon, keyword);
		
		DBConnection connection = DBConnectionFactory.getConnection();
		List<Item> items = connection.searchItems(lat, lon, keyword);
		connection.close();
		
		Set<String> favorite = connection.getFavoriteItemIds(userId);
		JSONArray array = new JSONArray();
		try {
			for(Item item : items) {
				JSONObject obj = item.toJSONObject();
				
				obj.put("favorite", favorite.contains(item.getItemId()));
				array.put(obj);
			}
		}catch(Exception e) { //���ﲻ���ٳ���JSONException��������Ϊtry����û��new�ˣ��Ͳ�����JSON���쳣��
			e.printStackTrace();
		}
		
		RpcHelper.writeJsonArray(response, array);
//		String username = "";
//		if(request.getParameter("username") != null) {
//			username = request.getParameter("username");
//		}
//		JSONArray array = new JSONArray();
//		try {
//			array.put(new JSONObject().put("username", username));
//			array.put(new JSONObject().put("username", "jack"));
//		}catch(JSONException e) {
//			e.printStackTrace();
//		}
//		RpcHelper.writeJsonArray(response, array); //����helper�����Ż�����
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
