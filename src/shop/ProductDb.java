package shop;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import conn.ConnQuery;
import conn.ConnUpdate;

public class ProductDb implements Serializable{
	private String sql;
	private ConnQuery conn= new ConnQuery();
	private ResultSet rs;
	private int query_count=0;
	
	public int getQuery_count() {return this.query_count;}
	public Collection<Product> getProducts() throws SQLException{
		rs=null;
		sql="SELECT * FROM teamweb2020.product WHERE prod_view=1";
		ArrayList<Product> prodList = new ArrayList<Product>();
		
		conn.setSql(sql);
		rs=conn.getRs();
		this.query_count=conn.getQuery_count();
		while(rs.next()) {
			Product product = new Product(rs.getInt(1), rs.getString(2),
					rs.getInt(3), rs.getString(4),
					rs.getInt(5), rs.getString(6));
			prodList.add(product);
		}
		return prodList;
	}
	public Product getProduct(int prod_id) throws SQLException{
		rs=null;
		sql="SELECT * FROM teamweb2020.product WHERE prod_view=1 AND prod_id="+prod_id;
		conn.setSql(sql);
		rs=conn.getRs();
		this.query_count=conn.getQuery_count();
		Product product=null;
		if (rs.next()) {
			product = new Product(rs.getInt(1), rs.getString(2),
					rs.getInt(3), rs.getString(4),
					rs.getInt(5), rs.getString(6));
		}
		return product;
	}
	
	public Collection<Product> searchProd(String keyboard) throws SQLException{
		rs=null;
		ArrayList<Product> prodList = new ArrayList<Product>();
		sql="SELECT * FROM teamweb2020.product WHERE prod_view=1 AND prod_name LIKE '%"+keyboard
				+"%'";
		conn.setSql(sql);
		rs=conn.getRs();
		while (rs.next()) {
			Product product = new Product(rs.getInt(1), rs.getString(2),
					rs.getInt(3), rs.getString(4),
					rs.getInt(5), rs.getString(6));
			prodList.add(product);
		}
		return prodList;
	}
	
	public boolean isAmountEnough(int prod_id,int quantity) throws SQLException{
		rs=null;
		boolean bEnough = false;
		sql="SELECT prod_size_stock FROM teamweb2020.product WHERE prod_id="+prod_id;
		conn.setSql(sql);
		rs=conn.getRs();
		
		while (rs.next()) {
			int amount = rs.getInt(1);
			if (amount >= quantity) {
				bEnough = true;
			}
		}
		return bEnough;
	}
	
	public synchronized void buyProducts(int mem_id,Cart cart,String orderList_name,String orderList_mphone,String oderList_address,String orderList_email) throws SQLException{
		ConnUpdate connUp = new ConnUpdate();
		ConnQuery connQry = new ConnQuery();
		Iterator<CartItem> it = cart.getItems().iterator();
		
		//寫入orderList table 訂單主檔=====
		//先抓id製作流水號
		sql="SELECT orderList_id FROM teamweb2020.orderList ORDER BY orderList_id DESC LIMIT 1;";
		connQry.setSql(sql);
		ResultSet rs=connQry.getRs();
		int orderList_id;
		if(rs.next()==true) {
			orderList_id=rs.getInt(1)+1;
		}else {
			orderList_id=1;
		}
		//寫入訂單主檔
		sql=String.format("INSERT INTO orderList(orderList_id,mem_id,orderList_name,orderList_mphone,orderList_address,orderList_email) VALUES(%d,%d,'%s','%s','%s','%s');", orderList_id,mem_id,orderList_name,orderList_mphone,oderList_address,orderList_email);
		connUp.setSql(sql);
		
			while (it.hasNext()) {		
				CartItem item = (CartItem) it.next();
				Product product = item.getProduct();
				int prod_id = product.getProd_id();
				int quantity = item.getQuantity();
				//每個商品庫存減少
				sql="UPDATE teamweb2020.product SET prod_size_stock=prod_size_stock-"
						+quantity+" WHERE prod_id ="+prod_id;
				connUp.setSql(sql);
				
				//每個商品寫入orderListDetail table 訂單明細====
				sql="INSERT INTO orderListDetail(orderList_id,prod_id,quantity)"
						+" VALUES("
						+orderList_id
						+", "+prod_id
						+", "+quantity
						+" );";
				connUp.setSql(sql);
			}
		
	}
}
