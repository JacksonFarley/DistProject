package queen;

import org.junit.Test;

import general.General;
import general.Byzantine;
import java.util.Random;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class QueenTest {

    private int ndecided(Queen[] qn){
        int counter = 0;
        Object v = null;
        Queen.retStatus ret;
        for(int i = 0; i < qn.length; i++){
            if(qn[i] != null){
                ret = qn[i].Status();
                if(ret.state == true) {
                    assertFalse("decided values do not match: i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void waitn(Queen[] qn, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(qn) >= wanted){
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

        int nd = ndecided(qn);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitn_iter(Queen[] qn, int wanted, int num_iterations)
    {
        int to = 1000;
        for(int i = 0; i < (10*num_iterations+5); i++){
            if(ndecided(qn) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        int nd = ndecided(qn);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    /*
    private void waitmajority(Queen[] qn){
        waitn(qn, (qn.length/2) + 1);
    }
    */

    private void cleanup(Queen[] qn){
        General.wait_millis(1000);
        for(int i = 0; i < qn.length; i++){
            if(qn[i] != null){
                qn[i].Kill();
            }
        }
    }

    private Queen[] initQueen(int nqueen, Float[] weights){
        String host = "127.0.0.1";
        String[] peers = new String[nqueen];
        int[] ports = new int[nqueen];
        Queen[] qn = new Queen[nqueen];
        for(int i = 0 ; i < nqueen; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < nqueen; i++){
            qn[i] = new Queen(i, peers, ports, weights);
        }
        return qn;
    }

    @Test 
    public void TestSingleQueen()
    {
        int nqueen = 1; 
        Float[] weights = {1.0f}; 
        Queen[] qn = initQueen(nqueen, weights);

        System.out.println("Test: Single Queen ...");

        qn[0].Start(1);
        
        waitn(qn, 1); 
        assertFalse("Validity not satisfied, qn has value" + qn[0].V, qn[0].V != 1);
        cleanup(qn); 

        qn = initQueen(nqueen, weights);

        qn[0].Start(0);

        waitn(qn, 1); 
        assertFalse("Validity not satisfied, qn has value" + qn[0].V, qn[0].V != 0);

        cleanup(qn);

        System.out.println("Variable weights"); 

        nqueen = 5; 
        Float[] new_weights = {0.1f,0.6f,0.3f,0.0f,0.0f}; 

        qn = initQueen(nqueen, new_weights);

        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(0);
        qn[4].Start(0);

        // only wait for one iteraion
        waitn_iter(qn, 5, 1); 
        assertFalse("Expecting 1" + qn[0].V, qn[0].V != 1);

        cleanup(qn);

        System.out.println("... Passed");


    }

    
    @Test
    public void TestBasic(){

        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        Queen[] qn = initQueen(nqueen, weights);

        System.out.println("Basic Test with 5 actors");
        // should be 0 (inconclusive weight followed by qn[0] queen)
        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(1);
        qn[4].Start(0);
        waitn(qn, nqueen);

        assertFalse("Expecting 0", qn[0].V != 0);
        cleanup(qn); 

        qn = initQueen(nqueen, weights);
        // should be 0 (consortium in iter 1 phase 1)
        qn[0].Start(1);
        qn[1].Start(0);
        qn[2].Start(0);
        qn[3].Start(0);
        qn[4].Start(0);

        waitn(qn, nqueen);
        assertFalse("Expecting 0", qn[0].V != 0);

        cleanup(qn); 

        qn = initQueen(nqueen, weights);
        // should be 1 (consortium in iter 1 phase 1)
        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(1);
        qn[3].Start(1);
        qn[4].Start(1);

        waitn(qn, nqueen);
        assertFalse("Expecting 1", qn[0].V != 1);

        cleanup(qn); 

        qn = initQueen(nqueen, weights);
        // should be 1 (inconclusive followed by queen value of 1)
        qn[0].Start(1);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(0);
        qn[4].Start(1);

        waitn(qn, nqueen);
        assertFalse("Expecting 1", qn[0].V != 1);

        System.out.println("... Passed");

        cleanup(qn);

    }

    @Test 
    public void TestKillBasic()
    {
        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        Queen[] qn = initQueen(nqueen, weights);

        System.out.println("Why Don't we kill Node 3");
        // should be 0 (inconclusive weight followed by qn[0] queen)
        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(1);
        qn[4].Start(0);
        
        General.wait_millis(4000);
        // Kill a process
        qn[3].Kill();

        waitn(qn, nqueen -1);

        //assertFalse("Expecting 0", qn[0].V != 0);
        cleanup(qn); 

        qn = initQueen(nqueen, weights);

        System.out.println("Off with her Head! Kill the queen");
        // should be 0 (inconclusive weight followed by qn[0] queen)
        qn[0].Start(0);
        qn[1].Start(1);
        qn[2].Start(0);
        qn[3].Start(1);
        qn[4].Start(0);
        
        General.wait_millis(4000);
        // Kill the queen
        qn[0].Kill();
        

        waitn(qn, nqueen -1);

        //assertFalse("Expecting 0", qn[0].V != 0);
        cleanup(qn); 

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
            Queen[] qn = initQueen(nqueen, weights);

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

            cleanup(qn); 
        }


    }

    @Test
    public void TestByzantineComplex()
    {
        final int nqueen = 10;
        Float[] weights = new Float[10];
        Arrays.fill(weights,0.1f); 

        Float[] weight_set0 = {0.1f,0.1f,0.1f,0.1f,0.1f,0.1f,0.1f,0.1f,0.1f,0.1f};
        Float[] weight_set1 = {0.1f,0.1f,0.1f,0.2f,0.05f,0.05f,0.1f,0.1f,0.1f,0.1f};
        Float[] weight_set2 = {0.075f,0.075f,0.06f,0.1f,0.075f,0.075f,0.1f,0.2f,0.14f,0.1f};
        Float[] weight_set3 = {0.07f,0.075f,0.075f,0.1f,0.075f,0.075f,0.1f,0.2f,0.1f,0.13f};

        Float[][] weight_master = {weight_set0, weight_set1,weight_set2,weight_set3};
        Float weight_byzantine; 
        Byzantine.ByzanType assignedType;
        int node;

        Random rand = new Random(3333); 
        int numRounds = 10;
        System.out.println("Byzantine Complex Test");

        for(int i = 0; i < numRounds; i++){
            System.out.println("\nRound "+i+"/"+numRounds);
            Float[] weight_selected = weight_master[rand.nextInt(4)];
            weight_byzantine = 0.0f;
            Queen[] qn = initQueen(nqueen, weight_selected);

            qn[0].Start(rand.nextInt(2));
            qn[1].Start(rand.nextInt(2));
            qn[2].Start(rand.nextInt(2));
            qn[3].Start(rand.nextInt(2));
            qn[4].Start(rand.nextInt(2));
            qn[5].Start(rand.nextInt(2));
            qn[6].Start(rand.nextInt(2));
            qn[7].Start(rand.nextInt(2));
            qn[8].Start(rand.nextInt(2));
            qn[9].Start(rand.nextInt(2));

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            weight_byzantine = weight_byzantine + weight_selected[node]; 
            while(weight_byzantine < 1.0f/4.0f){
                qn[node].set_byzantine(assignedType);
                System.out.println("Set Node "+node+" to byzatine type "+assignedType);
                assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
                node = rand.nextInt(nqueen);
                weight_byzantine = weight_byzantine + weight_selected[node];
            }
            
            waitn_iter(qn, nqueen, 4);

            cleanup(qn); 
            System.out.println("Round Cleared");
        }

        System.out.println("...Passed");

    }


    @Test 
    public void TestCoordinatedByzantineAttack()
    {
        final int nqueen = 9;
        Float[] weights = new Float[nqueen];
        Arrays.fill(weights,1.0f/nqueen); 

        Byzantine.ByzanType assignedType;

        System.out.println("Byzantine Coordinated Attack");

        for(int i = 0; i < 6; i++){
            System.out.println("\nRound "+i);
            Queen[] qn = initQueen(nqueen, weights);

            qn[0].Start(0);
            qn[1].Start(1);
            qn[2].Start(0);
            qn[3].Start(1);
            qn[4].Start(0);
            qn[5].Start(1);
            qn[6].Start(0);
            qn[7].Start(1);
            qn[8].Start(0);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis(1000);
            if(i == 5) {
                qn[0].Kill();
                qn[1].Kill();
                System.out.println("Kill 2");
            }else{
                assignedType = Byzantine.get_random_byzantine_type(i);
                qn[0].set_byzantine(assignedType);
                qn[1].set_byzantine(assignedType);
                System.out.println("Set to byzatine type "+assignedType);
            }
            
            waitn_iter(qn, nqueen -2, 4);

            cleanup(qn); 
        }
    }

    
}
