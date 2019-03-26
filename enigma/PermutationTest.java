package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;
import java.util.ArrayList;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void checkBreakDownCyclesSingle() {
        ArrayList<String> result = Permutation.breakDownCycles("(ABC)");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("ABC");
        assertEquals(expected, result);

    }

    @Test
    public void checkBreakDownCyclesMultiple() {
        ArrayList<String> result = Permutation.breakDownCycles("(ABC) (QUF)");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("ABC");
        expected.add("QUF");
        assertEquals(expected, result);

    }

    @Test
    public void checkBreakDownCyclesShort() {
        ArrayList<String> result = Permutation.breakDownCycles("(JZ)");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("JZ");
        assertEquals(expected, result);

    }

    @Test
    public void checkBreakDownCyclesLong() {
        ArrayList<String> result =
                Permutation.breakDownCycles("(AELTPHQXRU)"
                        + "(BKNW) (CMOY) (DFG) (IV) (JZ) (S)");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("AELTPHQXRU"); expected.add("BKNW"); expected.add("CMOY");
        expected.add("DFG"); expected.add("IV");
        expected.add("JZ"); expected.add("S");
        assertEquals(expected, result);

    }

    @Test
    public void checkPermutation() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        ArrayList<String> expected = new ArrayList<>();
        expected.add("ABC");
        expected.add("QUF");
        ArrayList<String> result = perm.cycles();
        assertEquals(expected, result);

    }

    @Test
    public void checkAddCycle() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        perm.addCycle("(DGN)");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("ABC");
        expected.add("QUF");
        expected.add("DGN");
        ArrayList<String> result = perm.cycles();
        assertEquals(expected, result);

    }

    @Test
    public void checkSize() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        assertEquals(26, perm.size());

    }

    @Test
    public void checkPermuteSingleFirst() {
        perm = new Permutation("(ABC)", UPPER);
        int result = perm.permute(0);
        assertEquals(1, result);

    }

    @Test
    public void checkPermuteSingleSecond() {
        perm = new Permutation("(ABC)", UPPER);
        int result = perm.permute(1);
        assertEquals(2, result);

    }

    @Test
    public void checkPermuteMultiple() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        int result = perm.permute(16);
        assertEquals(20, result);

    }

    @Test
    public void checkPermuteMapsToSelf() {
        perm = new Permutation("(B)", UPPER);
        int result = perm.permute(1);
        assertEquals(1, result);

    }

    @Test
    public void checkPermuteMapsToFront() {
        perm = new Permutation("(ABC)", UPPER);
        int result = perm.permute(2);
        assertEquals(0, result);

    }
    @Test
    public void checkPermuteBlank() {
        perm = new Permutation("", UPPER);
        int result = perm.permute(2);
        assertEquals(2, result);

    }

    @Test
    public void checkInvertSingleFirst() {
        perm = new Permutation("(ABC)", UPPER);
        int result = perm.invert(1);
        assertEquals(0, result);

    }

    @Test
    public void checkInvertSingleSecond() {
        perm = new Permutation("(ABC)", UPPER);
        int result = perm.invert(2);
        assertEquals(1, result);

    }

    @Test
    public void checkInvertMultiple() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        int result = perm.invert(20);
        assertEquals(16, result);

    }

    @Test
    public void checkInvertMapsToSelf() {
        perm = new Permutation("(B)", UPPER);
        int result = perm.invert(1);
        assertEquals(1, result);

    }

    @Test
    public void checkInvertMapsToBack() {
        perm = new Permutation("(ABC)", UPPER);
        int result = perm.invert(0);
        assertEquals(2, result);

    }

    @Test
    public void checkPermuteCharMultiple() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        char result = perm.permute('U');
        assertEquals('F', result);

    }

    @Test
    public void checkInvertCharMultiple() {
        perm = new Permutation("(ABC) (QUF)", UPPER);
        char result = perm.invert('U');
        assertEquals('Q', result);

    }

    @Test
    public void checkDerangement() {
        Permutation perm1 = new Permutation("(ABC) (QUF)", UPPER);
        assertTrue(perm1.derangement());

        Permutation perm2 = new Permutation("(A) (QUF)", UPPER);
        assertFalse(perm2.derangement());

        Permutation perm3 = new Permutation("(Q)", UPPER);
        assertFalse(perm3.derangement());

    }


}
