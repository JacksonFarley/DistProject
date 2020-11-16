package king;

import general.Byzantine;
import general.General;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class KingTest {

    private int ndecided(King[] kn){
        int counter = 0;
        Object v = null;
        King.retStatus ret;
        for(int i = 0; i < kn.length; i++){
            if(kn[i] != null){
                ret = kn[i].Status();
                if(ret.state == true) {
                    assertFalse("decided values do not match: i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void waitn(King[] kn, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(kn) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }
            if(to < 1000){
                to = to * 2;
            }
        }

        int nd = ndecided(kn);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitmajority(King[] kn){
        waitn(kn, (kn.length/2) + 1);
    }

    private void cleanup(King[] kn){
        for(int i = 0; i < kn.length; i++){
            if(kn[i] != null){
                kn[i].Kill();
            }
        }
    }

    private King[] initKing(int nking, Float[] weights){
        String host = "127.0.0.1";
        String[] peers = new String[nking];
        int[] ports = new int[nking];
        King[] kn = new King[nking];
        for(int i = 0 ; i < nking; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < nking; i++){
            kn[i] = new King(i, peers, ports, weights);
        }
        return kn;
    }

    @Test
    public void TestSingleKing()
    {
        final int nking = 1;
        final Float[] weights = {1.0f};
        King[] kn = initKing(nking, weights);

        System.out.println("Test: Single King ...");

        kn[0].Start(1);

        waitn(kn, 1);
        assertFalse("Validity not satisfied, kn has value" + kn[0].V, kn[0].V != 1);
        cleanup(kn);

        kn = initKing(nking, weights);

        kn[0].Start(0);

        waitn(kn, 1);
        assertFalse("Validity not satisfied, kn has value" + kn[0].V, kn[0].V != 0);

        cleanup(kn);
        System.out.println("... Passed");
    }


    @Test
    public void TestBasic(){

        final int nking = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f};

        King[] kn = initKing(nking, weights);

        System.out.println("Basic Test with 5 actors");
        // should be 0 (inconclusive weight followed by kn[0] king)
        kn[0].Start(0);
        kn[1].Start(1);
        kn[2].Start(0);
        kn[3].Start(1);
        kn[4].Start(0);
        waitn(kn, nking);

        assertFalse("Expecting 0", kn[0].V != 0);
        cleanup(kn);

        kn = initKing(nking, weights);
        // should be 0 (consortium in iter 1 phase 1)
        kn[0].Start(1);
        kn[1].Start(0);
        kn[2].Start(0);
        kn[3].Start(0);
        kn[4].Start(0);

        waitn(kn, nking);
        assertFalse("Expecting 0", kn[0].V != 0);

        cleanup(kn);

        kn = initKing(nking, weights);
        // should be 1 (consortium in iter 1 phase 1)
        kn[0].Start(0);
        kn[1].Start(1);
        kn[2].Start(1);
        kn[3].Start(1);
        kn[4].Start(1);

        waitn(kn, nking);
        assertFalse("Expecting 1", kn[0].V != 1);

        cleanup(kn);

        kn = initKing(nking, weights);
        // should be 1 (inconclusive followed by king value of 1)
        kn[0].Start(1);
        kn[1].Start(1);
        kn[2].Start(0);
        kn[3].Start(0);
        kn[4].Start(1);

        waitn(kn, nking);
        assertFalse("Expecting 1", kn[0].V != 1);

        System.out.println("... Passed");
        cleanup(kn);

    }

    @Test
    public void TestKillBasic()
    {
        final int nking = 7;
        final Float[] weights = {0.2f,0.1f,0.3f,0.2f,0.2f,0.0f,0.0f};

        King[] kn = initKing(nking, weights);

        System.out.println("Basic Kill process 0 and 1");
        // should be 0 (inconclusive weight followed by qn[0] queen)
        kn[0].Start(0);
        kn[1].Start(1);
        kn[2].Start(0);
        kn[3].Start(1);
        kn[4].Start(0);
        kn[5].Start(1);
        kn[6].Start(0);

        General.wait_millis(4000);
        // Kill two process
        kn[0].Kill();
        kn[1].Kill();

        waitn(kn, nking -2);

        assertFalse("Expecting 0", kn[2].V != 0);
        cleanup(kn);

        System.out.println("... Passed");
    }

    @Test
    public void TestByzantineBasic()
    {
        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f};
        Byzantine.ByzanType assignedType;
        int node;

        Random rand = new Random(2222);

        System.out.println("Byzantine Test");

        for(int i = 0; i < 15; i++){
            King[] qn = initKing(nqueen, weights);

            qn[0].Start(0);
            qn[1].Start(1);
            qn[2].Start(0);
            qn[3].Start(1);
            qn[4].Start(0);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            qn[node].set_byzantine(assignedType);
            System.out.println("Set Node "+node+" to byzatine type "+assignedType);

            waitn(qn, nqueen -1);
            System.out.println("Passed...");
            cleanup(qn);
        }
    }

    @Test
    public void TestByzantineDifferentWeights() {
        final int nqueen = 5;
        final Float[] weights = {0.3f,0.3f,0.2f,0.1f,0.1f};
        Byzantine.ByzanType assignedType;
        int node;

        Random rand = new Random(2222);

        System.out.println("Byzantine Test");

        for(int i = 0; i < 15; i++){
            King[] qn = initKing(nqueen, weights);

            qn[0].Start(0);
            qn[1].Start(1);
            qn[2].Start(0);
            qn[3].Start(1);
            qn[4].Start(0);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            qn[node].set_byzantine(assignedType);
            System.out.println("Set Node "+node+" to byzatine type "+assignedType);

            waitn(qn, nqueen -1);
            System.out.println("Passed...");
            cleanup(qn);
        }
    }

    @Test
    public void TestByzantineZeroWeights() {
        final int nqueen = 5;
        final Float[] weights = {0.4f,0.3f,0.2f,0.1f,0.0f};
        Byzantine.ByzanType assignedType;
        int node;

        Random rand = new Random(2222);

        System.out.println("Byzantine Test");

        for(int i = 0; i < 15; i++){
            King[] qn = initKing(nqueen, weights);

            qn[0].Start(0);
            qn[1].Start(1);
            qn[2].Start(0);
            qn[3].Start(1);
            qn[4].Start(0);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            qn[node].set_byzantine(assignedType);
            System.out.println("Set Node "+node+" to byzatine type "+assignedType);

            waitn(qn, nqueen -1);
            System.out.println("Passed...");
            cleanup(qn);
        }
    }




}
