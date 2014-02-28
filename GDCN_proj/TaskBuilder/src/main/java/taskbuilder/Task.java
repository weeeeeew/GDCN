package taskbuilder;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Class for tasks, compiling and executing Haskell code
 */
public class Task {

    private final String moduleName;
    private final String initData;

    public Task(String moduleName, String initData) {
        this.moduleName = moduleName;
        this.initData = initData;
    }

    // Compiles task code
    public void compile() throws IOException, InterruptedException, ExitFailureException {
	    PathManager pathman = PathManager.getInstance();
        //TODO Manage trust in a non hardcoded way
        String[] command = {pathman.getGhcPath(), "-o", pathman.getJobExecutablePath() + moduleName,
		        "-DMODULE=" + moduleName, "-i" + pathman.getJobCodePath(), pathman.getHeaderPath(),
                "-outputdir", pathman.getDumpPath(),
		        "-trust", "base", "-trust", "bytestring", "-trust", "binary"};
        Process proc = new ProcessBuilder(command).start();

        try {
            if (proc.waitFor() != 0) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(proc.getErrorStream(), writer, null);
                throw new ExitFailureException(writer.toString());
            }
        }
        catch (InterruptedException e) {
            proc.destroy();
            throw e;
        }
    }

    // Executes a task
    public byte[] execute() throws IOException, InterruptedException, ExitFailureException {
        PathManager pathman = PathManager.getInstance();
        String[] command = {pathman.getJobExecutablePath() + moduleName,
                pathman.getTaskInitDataPath() + initData};

        Process proc = new ProcessBuilder(command).start();

        try {
            if (proc.waitFor() != 0) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(proc.getErrorStream(), writer, null);
                throw new ExitFailureException(writer.toString());
            }
            else {
                //TODO Possibly wrap pure result data in a class

                outputStdErr(IOUtils.toString(proc.getErrorStream()));
                return IOUtils.toByteArray(proc.getInputStream());
            }
        }
        catch (InterruptedException e) {
            proc.destroy();
            throw e;
        }
    }

    // Compiles and executes a task
    public byte[] run() throws IOException, InterruptedException, ExitFailureException {
        String path = PathManager.getInstance().getJobExecutablePath() + moduleName;
        File executable = new File(path);
        if (executable.isDirectory()) {
            throw new IOException(path + " is a directory.");
        }
        if (!executable.exists()) {
            compile();
        }
        return execute();
    }
     public static void outputStdErr(String output){
        //TODO output in more general fashion
        System.out.println(output);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExitFailureException {
        //NOTE: This test only works for Unix with current GDCN.properties
        // Directories /tmp/GDCN and /tmp/GDCNDump must also exist, they will be used
	    PathManager.getInstance().loadFromFile(System.getProperty("user.dir") +
                File.separator + "TaskBuilder/resources/pathdata.prop");
        Task t = new Task("Prime", "2_2000.raw");
	    byte[] res = t.run();
        System.out.println(res);
    }
}
