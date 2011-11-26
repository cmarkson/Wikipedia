import java.sql.*;
import java.util.Scanner;



public class LinkAnalysis{
	  public static String countsize = "25";
	  private static Connection conn = null;
	  private static String url = "jdbc:mysql://localhost:3306/";
	  private static String dbName = "wikisql";
	  private static String driver = "com.mysql.jdbc.Driver";
	  private static String userName = "root"; 
	  private static String password = "admin";
	
	  
	public static void pageRankAnalysis(){
		  try {
			  Class.forName(driver).newInstance();
			  conn = DriverManager.getConnection(url+dbName,userName,password);

			  
			  Statement stmt = conn.createStatement();
			  Statement stmt2 = conn.createStatement();
			  Statement stmt3 = conn.createStatement();
			  Statement stmt4 = conn.createStatement();

			  stmt.execute("TRUNCATE TABLE ARTICLELIST;");
			  ResultSet rs;
			  
			  rs = stmt.executeQuery("select pagelinks.pl_from, page.page_id, count(page_id) from pagelinks join page where pagelinks.pl_title = page.page_title and page.page_id < "+ countsize +"  group by pl_from;");
			  while ( rs.next() ) {
			      String pl_from = rs.getString("pl_from");
			      String page_id = rs.getString("page_id");
			      String page_frq = rs.getString("count(page_id)");
			      System.out.println(pl_from +", "+ page_id + " " + page_frq);
			      stmt2.execute("INSERT IGNORE INTO articlelist (page_id) VALUES ('" + pl_from + "');");
     		      stmt3.execute("INSERT IGNORE INTO articlelist (page_id) VALUES ('" + page_id + "');");

			  }
			  ResultSet rs2;
			  rs2 = stmt.executeQuery("select page_id from articlelist");
			  System.out.println("-------------------------");
			  while ( rs2.next() ) {
			      String page_id = rs2.getString("page_id");
			      System.out.println(page_id);
			      
			  }
			  
			  System.out.println("-------------------------");
			  ResultSet rs3;
			  rs3=stmt4.executeQuery("select max(page_id) from articlelist;");
			  while ( rs3.next() ) {
				 String pl_max = rs3.getString("max(page_id)");
				  System.out.println(pl_max);
			  }
			  conn.close();

			  } catch (Exception e) {
			  e.printStackTrace();
			  }
			  
	
		
	}
	
	public static void suggestionGen(){
	      Scanner in = new Scanner(System.in);

		  try {
			  Class.forName(driver).newInstance();
			  conn = DriverManager.getConnection(url+dbName,userName,password);

			  
			  Statement stmt = conn.createStatement();
			  ResultSet rs;
			  int m = 800;
			  int n = 800;
			  int[][] freq = new int[m][n];
			 
			  
			  rs = stmt.executeQuery("select pagelinks.pl_from, page.page_id, count(page_id) from pagelinks join page where pagelinks.pl_title = page.page_title and page.page_id < "+Integer.toString(m) +" group by pl_from;");
			  while ( rs.next() ) {
			      String pl_from = rs.getString("pl_from");
			      String page_id = rs.getString("page_id");
			      String page_frq = rs.getString("count(page_id)");
			      int pl_fr=Integer.parseInt(pl_from);
			      int pl_to=Integer.parseInt(page_id);
			      int pl_frq=Integer.parseInt(page_frq);
			      if (pl_fr < m && pl_to < n){
			    	  freq[pl_fr][pl_to]=pl_frq;
			      }
			      
			  }
			  int article = 0;
			  while(true){
			  
			  //System.out.println("Choose an article (-1 to exit): ");
			  //int article=in.nextInt();
				  System.out.println();
			   article++;
			  if(article >m)
				  break;
			  
		      int maxarticle=0;
		      int maxfreq=0;
		      
		      
		      //making suggestion
		      for(int i=0; i<m; i++)
		    	  if(freq[i][article]>maxfreq){
		    		  maxfreq = freq[i][article];
		    		  maxarticle = i;
		    	  }
		    		
			  rs = stmt.executeQuery("select page_title from page where page_id ="+Integer.toString(article));
			  while ( rs.next() ) {
			      String articlestr = rs.getString("page_title");
			      System.out.println("Original article: "+articlestr);   
			  }
			  rs = stmt.executeQuery("select page_title from page where page_id ="+Integer.toString(maxarticle));
			  while ( rs.next() ) {
			      String articlesug = rs.getString("page_title");
			      System.out.println("Suggested article: "+articlesug);
			      System.out.println("Frequency count: " + maxfreq);
			      
			  }
			  
			  }
		      in.close();  
			  conn.close();

			  } catch (Exception e) {
			  e.printStackTrace();
			  }
		
	}
	
	
	//------------------------------------------------
	
	public static void specSuggestionGen(int freqlimit,int stepsize){
	      Scanner in = new Scanner(System.in);

		  try {
			  Class.forName(driver).newInstance();
			  conn = DriverManager.getConnection(url+dbName,userName,password);

			  
			  Statement stmt = conn.createStatement();
			  Statement stmt1 = conn.createStatement();
			  ResultSet rs, rs1;
			  int m = 10000000;
			  int stepsizecounter = 0;
			  int[] freq = new int[m];
			  int[] rank = new int[m];
			  int orgarticle;
			  int maxfreqtotal;
			  int maxfreq;
			  int maxarticle;
			  int maxrank;
			  int article;
			  while(true){
				  maxarticle = 0;
				  maxfreq=0;
				  maxfreqtotal=0;
				  maxrank=0;
				  stepsizecounter=0;
				  
				  System.out.println("Choose an article (quit to exit): ");
				   String request = in.nextLine();
				   if (request == "quit")
					   break;
				    rs=stmt.executeQuery("select page_id from page where page_title = '" + request + "';");
				    rs.next();
				    article = Integer.parseInt(rs.getString("page_id"));
				    System.out.println(article);
				    
				   orgarticle = article;
					  System.out.println();
				
				  
				  //reinitialize to 0
				  for(int i=0; i<m; i++){
			    	  freq[i]=0;
			    	  rank[i]=0;}
				  
				  while(maxfreqtotal<freqlimit && stepsizecounter<stepsize){
					  System.out.println("Step: "+stepsizecounter);
					  rs = stmt.executeQuery("select pagelinks.pl_from, page.page_id, count(page_id) from pagelinks join page where pagelinks.pl_title = page.page_title and page.page_id = " +article +" group by pl_from;");
					  while ( rs.next() ) {
						  String pl_from = rs.getString("pl_from");
						  // String page_id = rs.getString("page_id");
						  String page_frq = rs.getString("count(page_id)");
						  int pl_fr=Integer.parseInt(pl_from);
						  // int pl_to=Integer.parseInt(page_id);
						  int pl_frq=Integer.parseInt(page_frq);
						  
						  if (pl_fr < m){
							  if(pl_frq>1){
							  rs1 = stmt1.executeQuery("select count(page_id) from pagelinks join page where pagelinks.pl_title = page.page_title and page.page_id = " +article +" group by page_id;");
							  rs1.next();
							  rank[pl_fr] = Integer.parseInt(rs1.getString("count(page_id)"));
							  }
							  freq[pl_fr]=pl_frq;
						  }
					  }
				    
			      maxarticle=0;
			      maxrank =0;
			      maxfreq=0;
			      
			      //making suggestion
			      for(int i=0; i<m; i++){
			    	  if(i==article)
			    		  i++;
			    	  if(freq[i]>maxfreq || rank[i]>maxrank){
			    		  maxfreq = freq[i];
			    		  maxrank = rank[i];
			    		 
			    		  maxarticle = i;
			    	  }
			      }
			      
			      rs = stmt.executeQuery("select page_title from page where page_id ="+Integer.toString(maxarticle));
				  while ( rs.next() ) {
				      String articlestr = rs.getString("page_title");
				      System.out.println(stepsizecounter + " article: "+articlestr);   
				      
				      
				  }
		      
			      article = maxarticle;
			      maxfreqtotal += maxfreq;
			      stepsizecounter++;
			      System.out.println("Frequency total: "+ maxfreqtotal);
			      System.out.println("Rank: "+ maxrank);
				  }
			  
				  
			  rs = stmt.executeQuery("select page_title from page where page_id ="+Integer.toString(orgarticle));
			  while ( rs.next() ) {
			      String articlestr = rs.getString("page_title");
			      System.out.println("\n\nOriginal article: "+articlestr);   
			  }
			  rs = stmt.executeQuery("select page_title from page where page_id ="+Integer.toString(maxarticle));
			  while ( rs.next() ) {
			      String articlesug = rs.getString("page_title");
			      System.out.println("Suggested article: "+articlesug);
			      System.out.println("Frequency count: " + maxfreq);
			      System.out.println("Rank: "+ maxrank);
			      
			  }
			  System.out.println("\n\n");
			  }
		      in.close();  
			  conn.close();

			  } catch (Exception e) {
		
			  }
		
	}
  public static void main(String[] args) {

	  specSuggestionGen(7,5);
	 
	  
}}