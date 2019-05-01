package se.independent.dbclassloader.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which installs the artifact into a DbCL database
 */
@Mojo( name = "dbcl_compile", defaultPhase = LifecyclePhase.COMPILE )
public class CompileMojo
    extends AbstractDbCLMojo
{
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.outputDirectory}", property = "outputDir", required = true )
    private File outputDirectory;

    @Parameter( defaultValue  = "${project.build.finalName}", property = "dbclPathElement", required = true)
    private String pathElement;

    
    

    public void execute() throws MojoExecutionException {
    	getLog().debug("> execute()");
    	
    	getLog().info("- execute() build.directory=" + outputDirectory.getPath());

        File f = outputDirectory;

        if ( !f.exists() || !f.isDirectory() ) {
        	throw new MojoExecutionException( outputDirectory + " does not exist " );
        }
                       
        connect();
        
       try {
	    	if (f.canRead() && f.isDirectory()) {
	    		getLog().info("- execute() importDir as " + pathElement);
        		_manager.importDir(f.getAbsolutePath(), pathElement);
	    	}
       } catch (IOException iox) {
    	   throw new MojoExecutionException("# execute()", iox);
        } finally {
        	if (_manager != null) _manager.close();
        	_manager = null;
        }
    	getLog().debug("< execute()");
    }

}
