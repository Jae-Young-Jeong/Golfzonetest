// charse=UTF-8
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;



public class MySql1 {
    Pattern pattern = Pattern.compile(":([_0-9a-zA-Z]+)");
    String sqlTable = "sysSqls";
    String constr  = "jdbc/db";
    
    
    public MySql1() {

	}

    public MySql1(String dbconstr) {
    	this.constr = dbconstr;
	}

    
    
	public String sqlidToJson(String sqlid, HttpServletRequest request, HttpServletResponse response ) {
		String sql 
=  	"select	M.sysSql_id, M.sqlid, M.sqlnm,  D.sysSqlDtl_id,  D.sta_ymd, D.end_ymd, D.sqlText \n" 
+	"  from	sysSql		M\n"
+	"  join	sysSqlDtl	D on (D.sysSql_id = M.sysSql_id  and	date(now()) between D.sta_ymd and D.end_ymd)\n"
+	" where	M.useYn = 'Y'\n"
+	"   and	M.sqlid = '" + sqlid + "'" 
;
		try (Connection cn =  MyDBConnection.getConnection(this.constr); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql); ) {
			
			while(rs.next()) {
				sql  = rs.getString("sqlText");
			}
			
		} catch(Exception e) {
			System.out.println( "exception.message=" + e.getMessage());
			return null;
		}
		System.out.println("------------------------------------------------------------------------------------------------");
		System.out.println(sqlid);
		System.out.println("------------------------------------------------------------------------------------------------");

		return sqlToJson(sql, request, response);
	}

	
	public String sqlToJson(String sql, HttpServletRequest request, HttpServletResponse response ) {
		String newsql1 = null; 
		String newsql2 = sql;
		StringBuilder buf = new StringBuilder();
		
	    Matcher matcher = pattern.matcher(sql);
	    ArrayList<String> params = new ArrayList<String>();
 

	    int count = 0;
	    while(matcher.find()) {
	        count++;
	        System.out.print("(" + count + ")" +  matcher.group().substring(1) + ",");
	        params.add(matcher.group().substring(1));
	    }
		
	    newsql1 = sql.replaceAll(":([_0-9a-zA-Z]+)", "?");
//	    System.out.println("new_sql=" + newsql);
		
		try (Connection cn =  MyDBConnection.getConnection(constr); Statement st = cn.createStatement();  ) {
			PreparedStatement pstmt = cn.prepareStatement(newsql1);
			ResultSet rs = null;
			String objDelimiter = "";
			int rowCount = 0;
			
			
			for(int n = 0; n < params.size(); n++) {
//				System.out.println(String.valueOf(n) + "=" + params.get(n));
//				System.out.println(params.get(n) + "=" +  (String) request.getParameter( params.get(n)) );
				
				pstmt.setString(n+1, (String) request.getParameter( params.get(n)) );
				newsql2 = newsql2.replaceAll(":" + params.get(n), "'" + (String) request.getParameter( params.get(n)) + "'");
			}
System.out.println();
System.out.println(newsql2);

			rs = pstmt.executeQuery();
			
			ResultSetMetaData meta = rs.getMetaData();

			buf.append("{\"items\":[");
			rowCount = 0;
			while(rs.next()) {
				int cc = meta.getColumnCount();
				String delimiter;
				
				buf.append(objDelimiter).append("{");
				delimiter = "";
				for(int n=0; n< cc; n++) {
					buf.append(delimiter).append("\"").append(meta.getColumnName(n+1)).append("\":\"").append(rs.getString(n+1)).append("\"");
					delimiter = ",";
				}
				
				buf.append("}");
				objDelimiter = ",";
				rowCount++;
			}
			buf.append("], \"rowCount\":\"" + String.valueOf(rowCount)  + "\"}");

			
		} catch(Exception e) {
			System.out.println( "exception.message=" + e.getMessage());
			System.out.println(newsql2);
			return null;
		}
		
//		System.out.println(buf.toString());
		return (buf.toString());
	}

}




