package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author A.R. LOEFFLER
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;

    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }


    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = posn;
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        int convert = _permutation.alphabet().toInt(cposn);
        set(convert);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int tmp = p + setting();
        tmp = _permutation.permute(_permutation.wrap(tmp));
        return _permutation.wrap(tmp - setting());

    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int tmp = e + setting();
        tmp = _permutation.invert(_permutation.wrap(tmp));
        return _permutation.wrap(tmp - setting());
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {

    }
    /** Advance me one position, if possible.
     * Only to be used for the fast rotor. */
    void forceAdvance() {

    }



    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** Return my notches. */
    int[] notches() {
        return _notches;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** The setting of this rotor. */
    private int _setting;

    /** The array of notches for this rotor. */
    private int[] _notches;

}
