package replica;

import java.io.Serializable;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HalfLeif on 2014-04-08.
 */
public class ReplicaTimer implements Serializable, Cloneable {

    private final long UPDATE_TIME;

    private final PriorityQueue<ReplicaTimeout> queue = new PriorityQueue<>();
    private Outdater outdater;

    /**
     * Creates a ReplicaTimer with outdater null. Use setter.
     */
    public ReplicaTimer(){
        this(null);
    }

    public ReplicaTimer(Outdater outdater) {
        this.outdater = outdater;
        UPDATE_TIME =  1000*60;
    }

    /**
     * This constructor should only be used for testing!
     * @param outdater Listener
     * @param updateTime Milliseconds for polling
     */
    public ReplicaTimer(Outdater outdater, long updateTime) {
        this.outdater = outdater;
        UPDATE_TIME =  updateTime;
    }

    /**
     *
     * @param outdater ReplicaManager that can accept outdate orders
     */
    public void setOutdater(Outdater outdater) {
        this.outdater = outdater;
    }

    /**
     *
     * @return Clone without "outdater" set
     */
    @Override
    public ReplicaTimer clone() {
        ReplicaTimer clone = new ReplicaTimer(null, this.UPDATE_TIME);
        clone.queue.addAll(this.queue);

        //Outdater must be null in order to Serialize
        clone.outdater = null;
        return clone;
    }

    /**
     * Clock that updates this timer. This class must be Serializable which {@link java.util.Timer} isn't.
     * @return Runnable
     */
    public Runnable createUpdater(){
        return new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        update();
                    }
                }, UPDATE_TIME/2, UPDATE_TIME);
            }
        };
    }

    /**
     *
     * @param replicaID ID of a replica
     * @param date Expiration date of the replica
     */
    public synchronized void add(String replicaID, Date date){
        ReplicaTimeout replicaTimeout = new ReplicaTimeout(replicaID, date);
        queue.add(replicaTimeout);
    }

    /**
     * Called by clock to check the queue.
     *
     * Resembles busy-wait. Problem is, {@link java.util.Timer} is not serializable...
     */
    private synchronized void update(){
        final Date currentTime = new Date();
        if(queue.peek()==null){
//            System.out.println("ReplicaTimer: queue empty on update");
            return;
        }
//        long timeDiff = queue.peek().getDate().getTime()-currentTime.getTime();
//        System.out.println("TimeDiff to next element: "+timeDiff);
        while(queue.peek()!=null && queue.peek().getDate().compareTo(currentTime) < 0){
            ReplicaTimeout outdated = queue.remove();
//            System.out.println("Outdated called!");
            outdater.replicaOutdated(outdated.getReplicaID());
        }
    }


    private static class ReplicaTimeout implements Serializable, Comparable<ReplicaTimeout>{

        private final Date date;
        private final String replicaID;

        private ReplicaTimeout(String replicaID, Date date) {
            this.date = date;
            this.replicaID = replicaID;
        }

        public String getReplicaID() {
            return replicaID;
        }

        public Date getDate() {
            return date;
        }

        /**
         *
         * @param replicaTimeout Other ReplicaTimer
         * @return comparison
         */
        @Override
        public int compareTo(ReplicaTimeout replicaTimeout) {
            if(replicaTimeout==null){
                return 1;
            }
            return date.compareTo(replicaTimeout.date);
        }
    }
}
