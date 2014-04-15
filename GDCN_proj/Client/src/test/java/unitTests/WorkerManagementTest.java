package unitTests;

import se.chalmers.gdcn.control.WorkerNodeManager;
import se.chalmers.gdcn.network.WorkerID;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Leif on 2014-04-01.
 */
public class WorkerManagementTest {

    private WorkerNodeManager workerNodeManager;

    private KeyPair keyPairA;
    private WorkerID workerA;
    private WorkerID myWorkerID;

    private final static int DEMOTE_REPUTATION = 3;

    @BeforeClass
    public void setupClass() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        keyPairA = generator.generateKeyPair();
        workerA = new WorkerID(keyPairA.getPublic());

        myWorkerID = new WorkerID(generator.generateKeyPair().getPublic());
    }

    @BeforeMethod
    public void setupTest(){
        workerNodeManager = new WorkerNodeManager(myWorkerID, DEMOTE_REPUTATION, WorkerNodeManager.DisciplinaryAction.REMOVE);
    }

    @Test
    public void registerTest() throws NoSuchAlgorithmException {
        assert ! workerNodeManager.hasWorkerReputation(workerA);
        assert workerNodeManager.registerWorker(workerA);

        assert ! workerNodeManager.hasWorkerReputation(workerA);
        workerNodeManager.promoteWorker(workerA);

        assert workerNodeManager.hasWorkerReputation(workerA);
        assert workerNodeManager.hasWorkerReputation(new WorkerID(keyPairA.getPublic()));
    }

    @Test
    public void removeTest(){
        assert workerNodeManager.registerWorker(workerA);
        workerNodeManager.reportWorker(workerA, WorkerNodeManager.DisciplinaryAction.REMOVE);

        assert ! workerNodeManager.hasWorkerReputation(workerA);
    }

    @Test
    public void reputationTest(){
        workerNodeManager.registerWorker(workerA);

        assert 0 == workerNodeManager.getReputation(workerA);
        workerNodeManager.promoteWorker(workerA);
        assert 1 == workerNodeManager.getReputation(workerA);

        workerNodeManager.reportWorker(workerA, WorkerNodeManager.DisciplinaryAction.DEMOTE);
        assert 1-DEMOTE_REPUTATION == workerNodeManager.getReputation(workerA);
    }

    @Test
    public void trustHimselfTest(){
        assert workerNodeManager.hasWorkerReputation(myWorkerID);
    }

    @Test
    public void exceptionTestReputation(){
        assert 0 == workerNodeManager.getReputation(workerA);
    }

    @Test
    public void exceptionTestPromote(){
        boolean exceptionThrown = false;
        try{
            workerNodeManager.promoteWorker(workerA);
        } catch (IllegalArgumentException e){
            exceptionThrown = true;
        }
        assert exceptionThrown;
    }

}
