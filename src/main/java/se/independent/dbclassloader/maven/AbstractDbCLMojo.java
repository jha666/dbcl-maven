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

/*
           <plugin>
		        <groupId>se.independent</groupId>
		        <artifactId>dbcl-maven-plugin</artifactId>
		        <version>0.0.2</version>
		        <dependencies>
		        	<dependency>
					    <groupId>redis.clients</groupId>
					    <artifactId>jedis</artifactId>
					    <version>2.9.0</version>
					</dependency>
					<dependency>
    					<groupId>org.xerial</groupId>
    					<artifactId>sqlite-jdbc</artifactId>
    					<version>3.27.2</version>
					</dependency>
				</dependencies>
                <configuration>
                	<!--  -->
            		<jdbcURL>jdbc:sqlite:C:/Users/jha/dbcl-sqlite.db</jdbcURL>
           			<driverURL>jar:file:/c:/Users/jha/sqlite-jdbc-3.27.2.1.jar!/</driverURL>
           			<driverClass>org.sqlite.JDBC</driverClass>
           			<!--  -->
           			<!--  
            		<jdbcURL>redis://192.168.1.36:6379/0</jdbcURL>
           			<driverURL>jar:file:/c:/Users/jha/jedis-2.9.0.jar!/</driverURL>
           			<driverClass>redis.clients.jedis.BinaryJedis</driverClass>
           			-->
                    <dbUser>DBCLASSLOAD</dbUser>
                    <dbPasswd>Tr1ss</dbPasswd>
                </configuration>
		        <executions>
		          <execution>
		            <phase>install</phase>
		            <goals>
		              <goal>dbcl_install</goal>
		            </goals>
		            </execution>
		        </executions>
      </plugin>

 */
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
