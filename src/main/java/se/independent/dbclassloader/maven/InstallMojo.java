package se.independent.dbclassloader.maven;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which installs the artifact into a DbCL database
 */
@Mojo( name = "dbcl_install", defaultPhase = LifecyclePhase.INSTALL )
public class InstallMojo
    extends AbstractDbCLMojo
{
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File outputDirectory;
 
    @Parameter( defaultValue  = "${project.build.finalName}", property = "dbclPathElement", required = true)
    private String pathElement;

    @Parameter( defaultValue = "${project.packaging}")
    private String packaging;
    
 
    public void execute() throws MojoExecutionException {
    	getLog().debug("> execute()");
    	
    	getLog().info("- execute() build.directory=" + outputDirectory.getPath());

        File f = outputDirectory;

        if ( !f.exists() || !f.isDirectory() ) {
        	throw new MojoExecutionException( "project.build.directory does not exist " );
        }
        
        //loadDriver();
        
        connect();
        
       try {
	        File w = new File(f.getPath() + File.separator + pathElement + "." + packaging);
	    	if (w.canRead() && w.isFile()) {
	    		getLog().info("- execute() war=" + w.getPath());
	    		if ("war".equalsIgnoreCase(packaging)) {
	    			_manager.importWar(new JarFile(w));
	    		} 
	    		else if ("jar".equalsIgnoreCase(packaging)) {
	    			_manager.importJar(new JarFile(w));
	    		} else {
	    			getLog().warn("Dbcl_install: ignoring " + w.getAbsolutePath());
	    		}
	    	}
       } catch (IOException iox) {
    	   throw new MojoExecutionException("# execute()", iox);
       } finally {
    	   try{ _manager.close(); } catch (Exception ign) {}
       }
    	getLog().debug("< execute()");
    }
}
