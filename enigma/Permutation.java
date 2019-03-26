package enigma;

import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author A.R. LOEFFLER
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cycles = breakDownCycles(cycles);

    }

    /**Breaks down and returns the string CYCLES into an array list of strings,
     *  where each string is the conversion of a cycle from CYCLES
     *  from the form (ABCD) to the form ABCD. */
    public static ArrayList<String> breakDownCycles(String cycles) {

        ArrayList<String> result = new ArrayList<>();
        ArrayList<Integer> opens = new ArrayList<>();
        ArrayList<Integer> closeds = new ArrayList<>();
        for (int i = 0; i < cycles.length(); i += 1) {
            if (cycles.charAt(i) == '(') {
                opens.add(i);
            }
            if (cycles.charAt(i) == ')') {
                closeds.add(i);
            }
        }
        for (int i = 0; i < opens.size(); i += 1) {
            result.add(cycles.substring(opens.get(i) + 1, closeds.get(i)));
        }
        return result;
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    public void addCycle(String cycle) {
        ArrayList<String> holder = breakDownCycles(cycle);
        for (int i = 0; i < holder.size(); i++) {
            _cycles.add(holder.get(i));
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char ch = _alphabet.toChar(p);
        char toPermute = ch;
        for (int i = 0; i < _cycles.size(); i += 1) {
            for (int j = 0; j < _cycles.get(i).length(); j += 1) {
                char curr = _cycles.get(i).charAt(j);
                int cellLength = _cycles.get(i).length();
                if (curr == ch) {
                    if (cellLength == 1) {
                        toPermute = curr;
                    } else {
                        if (j + 1 == cellLength) {
                            toPermute = _cycles.get(i).charAt(0);
                        } else {
                            toPermute = _cycles.get(i).charAt(j + 1);
                        }
                    }
                }
            }
        }
        return _alphabet.toInt(toPermute);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char ch = _alphabet.toChar(c);
        char toPermute = ch;
        for (int i = 0; i < _cycles.size(); i += 1) {
            for (int j = 0; j < _cycles.get(i).length(); j += 1) {
                char curr = _cycles.get(i).charAt(j);
                int cellLength = _cycles.get(i).length();
                if (curr == ch) {
                    if (cellLength == 1) {
                        toPermute = curr;
                    } else {
                        if (j - 1 < 0) {
                            toPermute = _cycles.get(i).charAt(cellLength - 1);
                        } else {
                            toPermute = _cycles.get(i).charAt(j - 1);
                        }
                    }
                }
            }
        }
        return _alphabet.toInt(toPermute);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int convert = _alphabet.toInt(p);
        int result = permute(convert);
        return _alphabet.toChar(result);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int convert = _alphabet.toInt(c);
        int result = invert(convert);
        return _alphabet.toChar(result);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Returns my cycles. */
    ArrayList<String> cycles() {
        return _cycles;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (String i : this._cycles) {
            if (i.length() == 1) {
                return false;
            }
        }
        return true;

    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** This permutation's cycles. */
    private ArrayList<String> _cycles;

}
