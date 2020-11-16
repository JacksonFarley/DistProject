package gradecast;

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
public class GradeCastTest {

    private int ndecided(GradeCast[] gc){
        int counter = 0;
        Object v = null;
        GradeCast.retStatus ret;
        for(int i = 0; i < gc.length; i++){
            if(gc[i] != null){
                ret = gc[i].Status();
                if(ret.state == true) {
                    assertFalse("decided values do not match: i=" + i + " v=" + v + " v1=" + ret.v, counter > 0 && !v.equals(ret.v));
                    counter++;
                    v = ret.v;
                }

            }
        }
        return counter;
    }

    private void waitn(GradeCast[] gc, int wanted){
        int to = 10;
        for(int i = 0; i < 30; i++){
            if(ndecided(gc) >= wanted){
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

        int nd = ndecided(gc);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    private void waitn_iter(GradeCast[] gc, int wanted, int num_iterations)
    {
        int to = 1000;
        for(int i = 0; i < (20*num_iterations+5); i++){
            if(ndecided(gc) >= wanted){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        int nd = ndecided(gc);
        assertFalse("too few decided; ndecided=" + nd + " wanted=" + wanted, nd < wanted);

    }

    /*
    private void waitmajority(GradeCast[] gc){
        waitn(gc, (gc.length/2) + 1);
    }
    */

    private void cleanup(GradeCast[] gc){
        for(int i = 0; i < gc.length; i++){
            if(gc[i] != null){
                gc[i].Kill();
            }
        }
    }

    private GradeCast[] initGradeCast(int nqueen, Float[] weights){
        String host = "127.0.0.1";
        String[] peers = new String[nqueen];
        int[] ports = new int[nqueen];
        GradeCast[] gc = new GradeCast[nqueen];
        for(int i = 0 ; i < nqueen; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < nqueen; i++){
            gc[i] = new GradeCast(i, peers, ports, weights);
        }
        return gc;
    }

    @Test 
    public void TestSingleGradeCast()
    {
        int nqueen = 1; 
        Float[] weights = {1.0f}; 
        GradeCast[] gc = initGradeCast(nqueen, weights);

        System.out.println("Test: Single GradeCast ...");

        System.out.println("\nTest: Single Node Starting with 1");
        gc[0].Start(1);
        
        waitn_iter(gc, 1, 1); 
        assertFalse("Validity not satisfied, gc has value" + gc[0].V, gc[0].V != 1);
        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);

        System.out.println("\nTest: Single Node starting with 0");
        gc[0].Start(0);

        waitn_iter(gc, 1, 1);  
        assertFalse("Validity not satisfied, gc has value" + gc[0].V, gc[0].V != 0);

        cleanup(gc);

        System.out.println("\nTest: Variable weights with Node 1 as leader"); 

        nqueen = 5; 
        Float[] new_weights = {0.1f,0.6f,0.3f,0.0f,0.0f}; 

        gc = initGradeCast(nqueen, new_weights);

        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(0);
        gc[4].Start(0);

        // only wait for one iteraion
        waitn_iter(gc, 5, 1); 
        assertFalse("Expecting 1" + gc[0].V, gc[0].V != 1);

        cleanup(gc);

        System.out.println("... Passed");


    }

    
    @Test
    public void TestBasic(){

        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        GradeCast[] gc = initGradeCast(nqueen, weights);

        System.out.println("Basic Test with 5 actors");
        // should be 0 (inconclusive weight followed by gc[0] queen)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(1);
        gc[4].Start(0);
        waitn(gc, nqueen);

        assertFalse("Expecting 0", gc[0].V != 0);
        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);
        // should be 0 (consortium in iter 1 phase 1)
        gc[0].Start(1);
        gc[1].Start(0);
        gc[2].Start(0);
        gc[3].Start(0);
        gc[4].Start(0);

        waitn(gc, nqueen);
        assertFalse("Expecting 0", gc[0].V != 0);

        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);
        // should be 1 (consortium in iter 1 phase 1)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(1);
        gc[3].Start(1);
        gc[4].Start(1);

        waitn(gc, nqueen);
        assertFalse("Expecting 1", gc[0].V != 1);

        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);
        // should be 1 (inconclusive followed by queen value of 1)
        gc[0].Start(1);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(0);
        gc[4].Start(1);

        waitn(gc, nqueen);
        assertFalse("Expecting 1", gc[0].V != 1);

        System.out.println("... Passed");

        cleanup(gc);

    }

    @Test 
    public void TestKillBasic()
    {
        final int nqueen = 5;
        final Float[] weights = {0.2f,0.2f,0.2f,0.2f,0.2f}; 

        GradeCast[] gc = initGradeCast(nqueen, weights);

        System.out.println("Why Don't we kill Node 3");
        // should be 0 (inconclusive weight followed by gc[0] queen)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(1);
        gc[4].Start(0);
        
        General.wait_millis(4000);
        // Kill a process
        gc[3].Kill();

        waitn_iter(gc, nqueen -1, 2);

        //assertFalse("Expecting 0", gc[0].V != 0);
        cleanup(gc); 

        gc = initGradeCast(nqueen, weights);

        System.out.println("\nTest: Off with her Head! Kill the leader");
        // should be 0 (inconclusive weight followed by gc[0] queen)
        gc[0].Start(0);
        gc[1].Start(1);
        gc[2].Start(0);
        gc[3].Start(1);
        gc[4].Start(0);
        
        General.wait_millis(1000);
        // Kill the queen
        gc[0].Kill();
        

        waitn_iter(gc, nqueen -1, 2);

        //assertFalse("Expecting 0", gc[0].V != 0);
        cleanup(gc); 

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
            GradeCast[] gc = initGradeCast(nqueen, weights);

            gc[0].Start(0);
            gc[1].Start(1);
            gc[2].Start(0);
            gc[3].Start(1);
            gc[4].Start(0);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            gc[node].set_byzantine(assignedType);
            System.out.println("Set Node "+node+" to byzatine type "+assignedType);

            waitn_iter(gc, nqueen -1, 2);

            cleanup(gc); 
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

        System.out.println("Byzantine Complex Test");

        for(int i = 0; i < 1; i++){
            Float[] weight_selected = weight_master[rand.nextInt(4)];
            weight_byzantine = 0.0f;
            GradeCast[] gc = initGradeCast(nqueen, weight_selected);

            gc[0].Start(rand.nextInt(2));
            gc[1].Start(rand.nextInt(2));
            gc[2].Start(rand.nextInt(2));
            gc[3].Start(rand.nextInt(2));
            gc[4].Start(rand.nextInt(2));
            gc[5].Start(rand.nextInt(2));
            gc[6].Start(rand.nextInt(2));
            gc[7].Start(rand.nextInt(2));
            gc[8].Start(rand.nextInt(2));
            gc[9].Start(rand.nextInt(2));

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis((rand.nextInt(15)+1)*1000);
            assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
            node = rand.nextInt(nqueen);
            weight_byzantine = weight_byzantine + weight_selected[node]; 
            while(weight_byzantine < 1.0f/3.0f){
                gc[node].set_byzantine(assignedType);
                System.out.println("Set Node "+node+" to byzatine type "+assignedType);
                assignedType = Byzantine.get_random_byzantine_type(rand.nextInt(5));
                node = rand.nextInt(nqueen);
                weight_byzantine = weight_byzantine + weight_selected[node];
            }
            
            waitn_iter(gc, nqueen, 4);

            cleanup(gc); 
        }


    }

    @Test 
    public void TestCoordinatedByzantineAttack()
    {
        final int nqueen = 10;
        Float[] weights = new Float[nqueen];
        Arrays.fill(weights,0.1f); 

        Byzantine.ByzanType assignedType;

        System.out.println("Byzantine Coordinated Attack");

        for(int i = 0; i < 6; i++){
            System.out.println("\nRound "+i);
            GradeCast[] gc = initGradeCast(nqueen, weights);

            gc[0].Start(0);
            gc[1].Start(1);
            gc[2].Start(0);
            gc[3].Start(1);
            gc[4].Start(0);
            gc[5].Start(1);
            gc[6].Start(0);
            gc[7].Start(1);
            gc[8].Start(0);
            gc[9].Start(1);

            // wait for somewhere between 1 and 15 seconds
            General.wait_millis(1000);
            if(i == 5) {
                gc[0].Kill();
                gc[1].Kill();
                gc[2].Kill();
                System.out.println("Kill 3");
            }else{
                assignedType = Byzantine.get_random_byzantine_type(i);
                gc[0].set_byzantine(assignedType);
                gc[1].set_byzantine(assignedType);
                gc[2].set_byzantine(assignedType);
                System.out.println("Set to byzatine type "+assignedType);
            }
            
            waitn_iter(gc, nqueen -3, 4);

            cleanup(gc); 
        }
    }


   
}
