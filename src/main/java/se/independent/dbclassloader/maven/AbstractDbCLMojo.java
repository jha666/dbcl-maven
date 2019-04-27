package se.independent.dbclassloader.maven;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;
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
	    
	    @Parameter( property = "driverURL", defaultValue = "jar:file:/c:/Users/jha/postgresql-42.2.2.jar!/" , required = true)
	    private String driverURL;

	    @Parameter( property = "driverClass", defaultValue = "org.postgresql.Driver" , required = true)
	    private String driverClass;


	    protected void loadDriver() throws MojoExecutionException {
	        try {
	        	getLog().info("- execute() driverURL=" + driverURL + " [" + driverClass + "]");
		        URL u = new URL(driverURL);
				URLClassLoader ucl = new URLClassLoader(new URL[] { u });
				Driver d = (Driver) Class.forName(driverClass, true, ucl).newInstance();
				DriverManager.registerDriver(new DriverShim(d));
				
	        } catch (Exception x) {
	        	throw new MojoExecutionException("loadDriver: " + driverURL + " [" + driverClass + "]" , x);         	
	        }
	    }
	    
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
	    
	    
		class DriverShim implements Driver {
		    private Driver driver;

		    DriverShim(Driver d) {
		        this.driver = d;
		    }

		    @Override
		    public boolean acceptsURL(String u) throws SQLException {
		        return this.driver.acceptsURL(u);
		    }

		    @Override
		    public Connection connect(String u, Properties p) throws SQLException {
		        return this.driver.connect(u, p);
		    }

		    @Override
		    public int getMajorVersion() {
		        return this.driver.getMajorVersion();
		    }

		    @Override
		    public int getMinorVersion() {
		        return this.driver.getMinorVersion();
		    }

		    @Override
		    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
		        return this.driver.getPropertyInfo(u, p);
		    }

		    @Override
		    public boolean jdbcCompliant() {
		        return this.driver.jdbcCompliant();
		    }

		    @Override
		    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		        return driver.getParentLogger();
		    }

		}
}
