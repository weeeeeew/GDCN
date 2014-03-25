package files;

import command.communicationToUI.ClientInterface;
import control.TaskManager;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import network.Passer;
import taskbuilder.communicationToClient.TaskListener;

/**
 * Created by Leif on 2014-03-24.
 */
public class JobOwnerAwaits {

    private ClientInterface client;
    private Passer passer;
    private TaskManager taskManager;
//    private HashMap<String, AwaitTask> awaitTaskHashMap = new HashMap<>();

//    private static class AwaitTask{
//        private AbstractFileMaster.TaskMeta taskMeta;
//        private PeerAddress jobOwner;
//        private Long ref;
//
//        private AwaitTask(AbstractFileMaster.TaskMeta taskMeta, PeerAddress jobOwner, Long ref) {
//            this.taskMeta = taskMeta;
//            this.jobOwner = jobOwner;
//            this.ref = ref;
//        }
//    }

    public void tryWorkFor(PeerAddress jobOwner){
        //TODO request work
    }

    public void solveProof(PeerAddress jobOwner, Data indata, Long ref){
        //TODO solve proof
        passer.sendRequest(jobOwner, "SomeSolution"+ref);
    }

    public void workReceived(final PeerAddress jobOwner, final AbstractFileMaster.TaskMeta taskMeta, final Long ref){
        final String taskName = taskMeta.getTaskName();
//        awaitTaskHashMap.put(taskName, new AwaitTask(taskMeta, jobOwner, ref));

        final TaskListener taskComputed = new TaskListener() {
            @Override
            public void taskFinished(String taskName) {
                //TODO more nicely
                if(taskName.equals(taskMeta.getTaskName())){
                    System.out.println("ERROR in JobOwnerAwaits: Wrong listener!!!");
                }

                //TODO upload real result
                client.put2(taskMeta.getResultKey(), taskName, "The real result HERE please. Needs path manager...");
                passer.sendReply(jobOwner, "Success", ref);
            }

            @Override
            public void taskFailed(String taskName, String reason) {
                passer.sendReply(jobOwner, "Failure", ref);
            }
        };

        taskManager.startTask("SomeProjectName", taskName, client);
        //TODO compute tasks - now downloads meta using taskName instead of using this TaskMeta...
    }

//    private void workFinishedAndUploaded(String taskName){
//        AwaitTask awaitTask = awaitTaskHashMap.remove(taskName);
//        if(awaitTask == null){
//            //TODO report error
//            System.out.println("ERROR in JobOwnerAwaits: "+taskName+" was not found in awaited tasks!");
//            return;
//        }
//        passer.sendReply(awaitTask.jobOwner, "CompletedYourTask", awaitTask.ref);
//        System.out.println("Job owner may or not be notified about the finished task "+awaitTask.taskMeta.getTaskName());
//    }
}