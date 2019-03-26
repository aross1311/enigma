package enigma;

import java.util.Collection;
import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author A.R. LOEFFLER
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<>();
        for (Rotor i : allRotors) {
            _allRotors.add(i);
        }
        _usedRotors = new Rotor[numRotors];
        hasPlugboard = false;

    }

    /** Returns true if I have a plugboard. */
    boolean hasPlugboard() {
        return hasPlugboard;
    }

    /** Returns an array list of my usedRotors. */
    Rotor[] usedRotors() {
        return _usedRotors;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _usedRotors.length;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int ind = 0;
        for (int i = 0; i < rotors.length; i += 1) {
            for (int j = 0; j < _allRotors.size(); j += 1) {
                if (rotors[i].equals(_allRotors.get(j).name())) {
                    _usedRotors[ind] = _allRotors.get(j);
                    ind += 1;
                }
            }
        }
        _fastRotor = _usedRotors[_usedRotors.length - 1];
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 upper-case letters. The first letter refers to the
     *  leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        int i = 1;
        for (int j = 0; j < setting.length(); j += 1) {
            _usedRotors[i].set(setting.charAt(j));
            i += 1;
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = new Reflector("plugboard", plugboard);
        hasPlugboard = true;

    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing

     *  the machine. */
    int convert(int c) {

        ArrayList<Rotor> toAdv = new ArrayList<>();
        for (int i = _usedRotors.length - 2; i >= 0; i -= 1) {
            Rotor currRotor = _usedRotors[i];
            Rotor prevRotor = _usedRotors[i + 1];
            if (currRotor.rotates()) {
                if (prevRotor.atNotch()) {
                    toAdv.add(currRotor);
                    if (prevRotor != _fastRotor) {
                        if (!toAdv.contains(prevRotor)) {
                            toAdv.add(prevRotor);
                        }
                    }
                }
            }
        }
        toAdv.add(_fastRotor);
        for (Rotor r : toAdv) {
            r.advance();
        }
        int curr = c;
        if (hasPlugboard) {
            curr = _plugboard.convertForward(curr);
        }
        int currRotorInd = _usedRotors.length - 1;
        while (currRotorInd >= 0) {
            Rotor currRotor = _usedRotors[currRotorInd];
            curr = currRotor.convertForward(curr);
            currRotorInd -= 1;
        }
        currRotorInd = 1;
        while (currRotorInd < _usedRotors.length) {
            Rotor currRotor = _usedRotors[currRotorInd];
            curr = currRotor.convertBackward(curr);
            currRotorInd += 1;
        }
        if (hasPlugboard) {
            curr = _plugboard.convertBackward(curr);
        }

        return curr;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {

        msg = msg.toUpperCase();
        msg = msg.replaceAll(" ", "");

        if (msg.length() == 0) {
            return "";
        }

        String output = "";
        for (int i = 0; i < msg.length(); i += 1) {
            output += convertFromMessage(msg.charAt(i));
        }
        return output;
    }

    /** If character CH is a whitespace, returns whitespace,
     * otherwise returns the result of passing the
     * converted character (into an integer) into the convert
     * method and then converting that result into a String. */

    String convertFromMessage(char ch) {
        char curr = ch;
        char convertChar;
        if (curr == ' ') {
            return Character.toString(curr);
        }
        int convertInt = _alphabet.toInt(curr);
        convertInt = convert(convertInt);
        convertChar = _alphabet.toChar(convertInt);

        return Character.toString(convertChar);
    }

    /** Returns my list of all rotors. */
    ArrayList<Rotor> allRotors() {
        return _allRotors;
    }

    /** Common alphabet of my rotors. */

    /** My alphabet. */
    private final Alphabet _alphabet;

    /** My number of rotors. */
    private final int _numRotors;

    /** My number of pawls. */
    private final int _pawls;

    /** My list of all available rotors to select from. */
    private final ArrayList<Rotor> _allRotors;

    /** Rotors I'm actually using. */
    private Rotor[] _usedRotors;

    /** My plugboard. */
    private Reflector _plugboard;

    /** Returns true if I have a plugboard set up. */
    private boolean hasPlugboard;

    /** My furthest-right rotor. */
    private Rotor _fastRotor;

}
