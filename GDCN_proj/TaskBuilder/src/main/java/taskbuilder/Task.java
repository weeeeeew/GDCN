package taskbuilder;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for tasks, compiling and executing Haskell code
 */
class Task implements Runnable{

    private final String taskName;
    private final String moduleName;
    private final String initData;

    private final TaskListener listener;

    public Task(String taskName, String moduleName, String initData, TaskListener listener) {
        this.taskName = taskName;
        this.moduleName = moduleName;
        this.initData = initData;
        this.listener = listener;
    }

    /**
     * Compiles task code
     */
    public void compile(){

        PathManager pathman = PathManager.getInstance();

        List<File> dirs = new ArrayList<File>();
        dirs.add(new File(pathman.getJobExecutablePath()));
        dirs.add(new File(pathman.getDumpPath()));

        for(File dir : dirs){
            if(!dir.exists()){
                dir.mkdirs();
            }
        }

        //TODO Manage trust in a non hardcoded way
        String[] command = {"ghc", "-o", pathman.getJobExecutablePath() + moduleName,
		        "-DMODULE=" + moduleName, "-i" + pathman.getJobCodePath(), pathman.getHeaderPath(),
                "-outputdir", pathman.getDumpPath(),
		        "-trust", "base", "-trust", "bytestring", "-trust", "binary"};

        Process proc = null;
        try {
            proc = new ProcessBuilder(command).start();

            if (proc.waitFor() != 0) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(proc.getErrorStream(), writer, null);
                throw new ExitFailureException(writer.toString());
            }
        }
        catch (Exception e) {
            if (proc != null) {
                proc.destroy();
            }
            e.printStackTrace();
            listener.taskFailed(moduleName, e.getMessage());
        }
    }

    /**
     * Executes a task
     */
    public void execute(){
        PathManager pathman = PathManager.getInstance();
        String[] command = {pathman.getJobExecutablePath() + moduleName,
                pathman.getDumpPath() + taskName + ".result",
                pathman.getTaskInitDataPath() + initData};

        Process proc = null;

        try {
            proc = new ProcessBuilder(command).start();

            if (proc.waitFor() != 0) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(proc.getErrorStream(), writer, null);
                throw new ExitFailureException(writer.toString());
            }
            else {
                //TODO Possibly wrap pure result data in a class

//                outputStdErr(IOUtils.toString(proc.getErrorStream()));
//                outputStdErr(fromInstream(proc.getErrorStream()));

                // TODO Currently haskell doesn't print any to stderr...
                StringWriter writer = new StringWriter();
                IOUtils.copy(proc.getErrorStream(), writer, null);
                outputStdErr(writer.toString());

                listener.taskFinished(taskName);
//                return IOUtils.toByteArray(proc.getInputStream());
            }
        }
        catch (Exception e) {
            if (proc != null) {
                proc.destroy();
            }
            e.printStackTrace();
            listener.taskFailed(taskName, e.getMessage());
        }
    }

    /**
     * Compiles and executes a task
     */
    @Override
    public void run(){
        String path = PathManager.getInstance().getJobExecutablePath() + moduleName;
        File executable = new File(path);
        try {
            if (executable.isDirectory()) {
                throw new IOException(path + " is a directory.");
            }
            if (!executable.exists()) {
                compile();
            }
            execute();
        } catch (Exception e) {
            e.printStackTrace();
            listener.taskFailed(taskName, e.getMessage());
        }
    }

    public static String fromInstream(InputStream inputStream){

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


        String line;

        try {
            while( (line = reader.readLine()) != null){
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return stringBuilder.toString();
    }

    public static void outputStdErr(String output){
        //TODO output in more general fashion
        System.out.println("-- StdErr:");
        System.out.println(output);
    }

    public static void toFile(byte[] results){
        String path = PathManager.getInstance().getDumpPath();
        BufferedOutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(path));
            outputStream.write(results);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExitFailureException {
        //NOTE: This test only works for Unix with current GDCN.properties
        // Directories /tmp/GDCN and /tmp/GDCNDump must also exist, they will be used
	    PathManager.getInstance().loadFromFile(System.getProperty("user.dir") +
                File.separator + "TaskBuilder/resources/pathdata.prop");
        Task t = new Task("TaskName_Prime_1", "Prime", "2_2000.raw", new TaskListener() {
            @Override
            public void taskFinished(String taskName) {
                //TODO
            }

            @Override
            public void taskFailed(String taskName, String reason) {
                //TODO
            }
        });
	    t.run();
    }
}