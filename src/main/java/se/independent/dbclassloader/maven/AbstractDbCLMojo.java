package se.independent.dbclassloader.maven;

import java.util.Properties;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import se.independent.dbclassloader.DbClassLoaderManager;
import se.independent.dbclassloader.JDBCDbClassLoaderManager;
import se.independent.dbclassloader.JedisDbClassLoaderManager;
import se.independent.dbclassloader.MongoDBDbClassLoaderManager;
import se.independent.dbclassloader.Neo4jDbClassLoaderManager;
import se.independent.dbclassloader.SQLiteDbClassLoaderManager;

public abstract class AbstractDbCLMojo extends AbstractMojo {
	
		protected DbClassLoaderManager _manager;

	
	   	@Parameter( defaultValue = "jdbc:postgresql://127.0.0.1:5432/postgres", property = "jdbcURL" , required = true)
	    private String jdbcURL;

	    @Parameter( property = "dbUser", defaultValue = "DBCLASSLOAD" , required = true)
	    private String dbUser;
	    
	    @Parameter( property = "dbPasswd", defaultValue = "Tr1ss" , required = true)
	    private String dbPasswd;
	    
	    
	    protected void connect() throws MojoExecutionException {
	        try {
		        Properties p = new Properties();
		        p.setProperty("user", dbUser);
		        p.setProperty("password", dbPasswd);
		        
		        
				String[] URL = jdbcURL.split(":");
			     
			    switch (URL[0]) {
			    case "jdbc":   // jdbc:<subprotocol>:<subname>
			    	 String subprotocol = "";
			    	 String subname = "";
		   		 
			    	 try (Scanner scanner = new Scanner(jdbcURL)) {
			    		 scanner.findInLine("jdbc:(\\w+):(.*)");
			    		 MatchResult result = scanner.match();
			    		 subprotocol = result.group(1);
			    		 subname = result.group(2);        	     
			    	 }
		   		    		 
			    	 switch (subprotocol) {
			    	 case "sqlite":
			    		 _manager = new SQLiteDbClassLoaderManager();
			    		 break;
			    		 
			    	 default:
			    		 _manager = new JDBCDbClassLoaderManager();
			    		 break;
			    	 }
			    	 break;
			    	 
			     case "redis":
			    	 _manager = new JedisDbClassLoaderManager();
			    	 break;
			    	 
			     case "mongodb":
			    	 _manager = new MongoDBDbClassLoaderManager();
			    	 break;
			    	 
			     case "bolt":
			    	 _manager = new Neo4jDbClassLoaderManager();
			    	 break;
			    	 
			     default:
			    	 break;
			     }
		        
		    	getLog().info("- execute() jdbcURL=" + jdbcURL);
		        _manager.connect(jdbcURL, p);
		        _manager.prepare();
	        } catch (Exception x) {
	        	throw new MojoExecutionException("connect: " + jdbcURL + " " + dbUser, x);        	
	        }
	    }
	    
}
