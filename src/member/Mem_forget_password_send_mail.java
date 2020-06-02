package member;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import conn.ConnQuery;
import conn.ConnUpdate;


@WebServlet("/doForgetPasswordSendMail")
public class Mem_forget_password_send_mail extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out=response.getWriter();
		//=======
		String email=request.getParameter("inputEmail");
		//===
		String sql="SELECT * FROM TEAMWEB2020.member WHERE mem_mail='"+email+"';";
		ConnQuery connQry=new ConnQuery();
		connQry.setSql(sql);
		
		try {
			if (connQry.getQuery_count() > 0) {
				ResultSet rs = connQry.getRs();
				rs.first();
				String name = rs.getString(2);
				String id = String.valueOf(rs.getInt(1));
				//===
				int salt=(int)(Math.random()*100000);
				String saltStr=String.format("%06d", salt);
				sql=String.format("UPDATE TEAMWEB2020.MEMBER SET mem_chkcode = %s WHERE mem_id = %s;",saltStr,id);
				ConnUpdate connUp=new ConnUpdate();
				connUp.setSql(sql);
				//===
				String subject = "重設密碼";
				String content = String.format("%s您好!! 請您點選下述連結重設密碼<br>"
						+ "<a href='http://localhost:8080/TeamWeb/forgetpassReceiveMail.jsp?email=%s&enc=%s'>點此連結重設密碼</a>", name,email,saltStr);
				
				SendMail sMail = new SendMail();
				sMail.setTo(email);
				sMail.setSubject(subject);
				sMail.setContent(content);
				sMail.execute();
				response.sendRedirect("forgetpass.jsp?msg=1");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
