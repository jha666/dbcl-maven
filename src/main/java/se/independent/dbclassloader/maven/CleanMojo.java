package se.independent.dbclassloader.maven;

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which installs the artifact into a DbCL database
 */
@Mojo( name = "dbcl_clean", defaultPhase = LifecyclePhase.CLEAN )
public class CleanMojo
    extends AbstractDbCLMojo
{
 
    @Parameter( defaultValue  = "${project.build.finalName}", property = "dbclPathElement", required = true)
    private String pathElement;

    @Parameter( defaultValue = "${project.packaging}")
    private String packaging;
    

    public void execute() throws MojoExecutionException {
    	getLog().debug("> execute()");
    	 
        connect();

        try {
        getLog().info("- execute() path_element=" + pathElement);
		if ("war".equalsIgnoreCase(packaging)) {
			_manager.removetWar(pathElement);
		} 
		else if ("jar".equalsIgnoreCase(packaging)) {
			_manager.deleteJar(pathElement);
		} else {
			getLog().warn("Dbcl_clean: ignoring " + pathElement);
		}
       } catch (IOException iox) {
    	   throw new MojoExecutionException("# execute()", iox);
       } finally {
    	   try{ _manager.close(); } catch (Exception ign) {}
       }
    	getLog().debug("< execute()");
    }
}
