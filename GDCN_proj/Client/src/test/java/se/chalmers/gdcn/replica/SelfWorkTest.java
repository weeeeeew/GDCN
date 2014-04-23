package se.chalmers.gdcn.replica;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.chalmers.gdcn.communicationToUI.ClientInterface;
import se.chalmers.gdcn.control.TaskManager;
import se.chalmers.gdcn.control.TaskRunner;
import se.chalmers.gdcn.network.WorkerID;
import se.chalmers.gdcn.taskbuilder.communicationToClient.TaskListener;
import utils.TaskHolder;
import utils.TestUtils;
import utils.WorkerHolder;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Leif on 2014-04-21.
 */
public class SelfWorkTest {
    private ReplicaManager replicaManager;
    private Semaphore lock;

    private WorkerID workerA;

    @BeforeMethod
    public void setupMethod(){
        ReplicaManagerBuilder builder = new ReplicaManagerBuilder(WorkerHolder.getMyWorkerID(), emptyTaskManager());

        replicaManager = builder.create();
        replicaManager.setWorkSelfIfRequired(true);
        TestUtils.loadMeta(TaskHolder.getTaskA(), replicaManager);

        workerA = WorkerHolder.getWorkerA();

        lock = new Semaphore(0);
    }

    @Test
    public void oneTest(){
        ReplicaBox replicaBox = replicaManager.giveReplicaToWorker(workerA);
        replicaManager.replicaOutdated(replicaBox.getReplicaID());
        //...

        lock.acquireUninterruptibly();
    }

    @Test
    public void cloneTest() throws CloneNotSupportedException, IOException {
        ReplicaBox replicaBox = replicaManager.giveReplicaToWorker(workerA);
        ReplicaManager clone = replicaManager.shallowClone();
        assert clone != replicaManager;
        assert clone.getRunner() == null;

        //NotSerializableException?
        new Data(clone);

        assert clone.getReplicaResultKey(replicaBox.getReplicaID()).equals(replicaBox.getResultKey());
        assert null == replicaManager.giveReplicaToWorker(workerA);

        //NotSerializableException?
        new Data(clone);
    }

    @Test
    public void test() throws IOException, CloneNotSupportedException {
        SerializableRunner runner = new SerializableRunner(emptyTaskManager());
        runner.taskRunner = null;
        new Data(runner);

        new Data(runner.clone());
    }

    private static class NotSerializableClass{}

    private static class SerializableRunner implements TaskRunner, Serializable {

        private NotSerializableClass notSerializableClass = null;
        private TaskRunner taskRunner;

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        private SerializableRunner(TaskRunner taskRunner) {
            this.taskRunner = taskRunner;
        }

        @Override
        public void submit(Runnable runnable) {
            taskRunner.submit(runnable);
        }

        @Override
        public TaskListener getTaskListener() {
            return taskRunner.getTaskListener();
        }
    }

    private TaskManager emptyTaskManager(){
        return new TaskManager(new TaskListener() {
            @Override
            public void taskFinished(String taskName) {
                lock.release();
            }

            @Override
            public void taskFailed(String taskName, String reason) {
                lock.release();
                throw new AssertionError(reason);
            }
        }, new ClientInterface() {
            @Override
            public void install() {

            }

            @Override
            public void uninstall() {

            }

            @Override
            public void push(String jobName) {

            }

            @Override
            public void work(String address, int port) {

            }

            @Override
            public void addListener(PropertyChangeListener listener) {

            }

            @Override
            public void removeListener(PropertyChangeListener listener) {

            }

            @Override
            public void start(int port) {

            }

            @Override
            public void stop() {

            }

            @Override
            public void bootstrap(String host, int port) {

            }

            @Override
            public void put(String name, Data value) {

            }

            @Override
            public void put(Number160 key, Data value) {

            }

            @Override
            public void get(String name) {

            }

            @Override
            public void get(Number160 key) {

            }

            @Override
            public List<PeerAddress> getNeighbours() {
                return null;
            }

            @Override
            public List<PeerAddress> getOldNeighbours() {
                return null;
            }

            @Override
            public void reBootstrap() {

            }

            @Override
            public void send(String msg) {

            }

            @Override
            public void put2(String key, String domain, Object value) {

            }

            @Override
            public void get2(String key, String domain) {

            }

            @Override
            public void setNeighbourFile(String file) {

            }

            @Override
            public void clearNeighbourFile() {

            }

            @Override
            public void deleteNeighbourFile() {

            }

            @Override
            public void requestWork(int index) {

            }
        });
    }
}
