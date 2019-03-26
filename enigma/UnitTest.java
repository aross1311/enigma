package enigma;

import org.junit.Test;
import ucb.junit.textui;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static enigma.EnigmaException.error;

/** The suite of all JUnit tests for the enigma package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(PermutationTest.class, MovingRotorTest.class);
    }

    private ArrayList<Rotor> rotors = new ArrayList<>();

    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    @Test
    public void testScanningPatternLogic() {

        Scanner config;
        config = getInput("testing/correct/default.conf");
        String alphabetRange = config.nextLine();
        System.out.println(alphabetRange);

        String numRotors = config.next();
        String numPawls = config.next();
        System.out.println(numRotors);
        System.out.println(numPawls);

        String r1 = config.next();
        String typeNotch = config.next();
        System.out.println(r1);
        System.out.println(typeNotch);

        String cycle1 = config.nextLine();
        System.out.println(cycle1);
        Pattern patty = Pattern.compile("\\s+\\(\\w+\\)\\s+");
        Matcher matchy = patty.matcher(cycle1);

        if (matchy.find()) {
            System.out.println(matchy.group(0));

        }

        Scanner one = new Scanner("Hello World Yo");
        System.out.println(one.next());
        Scanner two = new Scanner(one.next());
        System.out.println(one.next());
        System.out.println(two.next());

    }

    private Machine readConfig() {
        try {
            String alphRange = _config.next();
            System.out.println(alphRange);
            _alphabet =
                    new CharacterRange(alphRange.charAt(0),
                            alphRange.charAt(2));
            _numRotors = Integer.parseInt(_config.next());
            System.out.println(_numRotors);
            _numPawls = Integer.parseInt(_config.next());
            System.out.println(_numPawls);
            while (_config.hasNextLine()) {
                _allRotors.add(readRotor());
                _previousAddedRotor += 1;
            }
            _allRotors.remove(_allRotors.size() - 1);

            return new Machine(_alphabet, _numRotors, _numPawls, _allRotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }


    private Rotor readRotor() {
        try {
            String name = _config.next().toUpperCase().trim();
            if (name.charAt(0) == '(') {
                String rest = _config.nextLine().trim();
                System.out.println("rest");
                System.out.println(rest);
                Rotor rotorPrior;
                rotorPrior = _allRotors.get(_previousAddedRotor - 1);
                rotorPrior.permutation().addCycle(name);
                rotorPrior.permutation().addCycle(rest);
                if (_config.hasNextLine()) {
                    return readRotor();
                }
                if (!_config.hasNextLine()) {
                    return new Reflector("tmp", new Permutation("", _alphabet));
                }
            }
            String typeNotch = _config.next().trim();
            String cycles = _config.nextLine().trim();
            Permutation perm = new Permutation(cycles, _alphabet);
            if (typeNotch.charAt(0) == 'M') {
                String notches = typeNotch.substring(1);
                return new MovingRotor(name, perm, notches);
            } else if (typeNotch.charAt(0) == 'N') {
                return new FixedRotor(name, perm);
            }
            return new Reflector(name, perm);

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    private void setUp(Machine M, String settings) {

        Scanner myScan = new Scanner(settings);

        String ast = myScan.next().trim();

        String[] rotorsToSet = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i += 1) {
            rotorsToSet[i] = myScan.next().trim();
        }
        M.insertRotors(rotorsToSet);
        String rotorsSettings = myScan.next().trim();
        M.setRotors(rotorsSettings);
        if (myScan.hasNext()) {
            String rest = myScan.nextLine().trim();
            Permutation perm = new Permutation(rest, _alphabet);
            M.setPlugboard(perm);
        }

    }

    @Test
    public void testMainConfiguration() {

        _config = getInput("testing/correct/default.conf");
        Machine m = readConfig();
        System.out.println(m.allRotors().get(9).permutation().cycles());
        System.out.println(m.allRotors().get(10).permutation().cycles());
        System.out.println(m.allRotors().get(11).permutation().cycles());

    }

    @Test
    public void testMainSettings() {

        Machine M = readConfig();
        _input = getInput("testing/correct/trivial1.inp");
        String ast = _input.next();
        String rotorsToSet = "";
        for (int i = 1; i <= M.numRotors(); i += 1) {
            rotorsToSet += _input.next();
        }
        M.setRotors(_input.next());
        if (_input.hasNext()) {
            String rest = _input.nextLine().trim();
            Permutation perm = new Permutation(rest, _alphabet);
            M.setPlugboard(perm);
        }


    }

    @Test
    public void testprintMessageLine() {

        String msg1 = "THIS SHOULD BE MADE INTO FIVE EACH";
        printMessageLine(msg1);

    }

    private void printMessageLine(String msg) {

        int charsSoFar = 0;
        String result = "";
        for (int i = 0; i < msg.length(); i += 1) {
            char curr = msg.charAt(i);
            if (curr != ' ') {
                if ((charsSoFar % 5 == 0) && (charsSoFar > 0)) {
                    result += " ";
                }
                result += curr;
                charsSoFar += 1;
            }
        }

        System.out.println(result);

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
    private ArrayList<Rotor> _allRotors = new ArrayList<>();
    private int _numRotors;
    private int _numPawls;
    private int _previousAddedRotor = 0;

}


