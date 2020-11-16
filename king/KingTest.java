package king;

import org.junit.Test;

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

/*
        System.out.println("Test: Many proposers, same value ...");
        for(int i = 0; i < nking; i++){
            kn[i].Start(1, 77);
        }
        waitn(kn, 1, nking);
        System.out.println("... Passed");

        System.out.println("Test: Many proposers, different values ...");
        kn[0].Start(2, 100);
        kn[1].Start(2, 101);
        kn[2].Start(2, 102);
        waitn(kn, 2, nking);
        System.out.println("... Passed");

        System.out.println("Test: Out-of-order instances ...");
        kn[0].Start(7, 700);
        try {
            Thread.sleep(10);
        } catch (Exception e){
            e.printStackTrace();
        }
        kn[0].Start(6, 600);
        kn[1].Start(5, 500);
        waitn(kn, 7, nking);
        kn[0].Start(4, 400);
        kn[1].Start(3, 300);
        waitn(kn, 6, nking);
        waitn(kn, 5, nking);
        waitn(kn, 4, nking);
        waitn(kn, 3, nking);
        System.out.println("... Passed");
        */
        cleanup(kn);

    }
    /*
    @Test
    public void TestDeaf(){

        final int nking = 5;
        King[] kn = initKing(nking);

        System.out.println("Test: Deaf proposer ...");
        kn[0].Start(0, "hello");
        waitn(kn, 0, nking);

        kn[1].ports[0]= 1;
        kn[1].ports[nking-1]= 1;
        kn[1].Start(1, "goodbye");
        waitmajority(kn, 1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        int nd = ndecided(kn, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != nking-2);

        kn[0].Start(1, "xxx");
        waitn(kn, 1, nking-1);
        try {
            Thread.sleep(1000);
        } catch (Exception e){
            e.printStackTrace();
        }
        nd = ndecided(kn, 1);
        assertFalse("a deaf peer heard about a decision " + nd, nd != nking-1);

        kn[nking-1].Start(1, "yyy");
        waitn(kn, 1, nking);
        System.out.println("... Passed");
        cleanup(kn);

    }

    @Test
    public void TestForget(){

        final int nking = 6;
        King[] kn = initKing(nking);

        System.out.println("Test: Forgetting ...");

        for(int i = 0; i < nking; i++){
            int m = kn[i].Min();
            assertFalse("Wrong initial Min() " + m, m > 0);
        }

        kn[0].Start(0,"00");
        kn[1].Start(1,"11");
        kn[2].Start(2,"22");
        kn[0].Start(6,"66");
        kn[1].Start(7,"77");

        waitn(kn, 0, nking);
        for(int i = 0; i < nking; i++){
            int m = kn[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        waitn(kn, 1, nking);
        for(int i = 0; i < nking; i++){
            int m = kn[i].Min();
            assertFalse("Wrong Min() " + m + "; expected 0", m != 0);
        }

        for(int i = 0; i < nking; i++){
            kn[i].Done(0);
        }

        for(int i = 1; i < nking; i++){
            kn[i].Done(1);
        }

        for(int i = 0; i < nking; i++){
            kn[i].Start(8+i, "xx");
        }

        boolean ok = false;
        for(int iters = 0; iters < 12; iters++){
            ok = true;
            for(int i = 0; i < nking; i++){
                int s = kn[i].Min();
                if(s != 1){
                    ok = false;
                }
            }
            if(ok) break;
            try {
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        assertFalse("Min() did not advance after Done()", ok != true);
        System.out.println("... Passed");
        cleanup(kn);


    }

    */
}
