package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author A.R. LOEFFLER
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine m = readConfig();

        boolean settingsRead = false;
        boolean firstRun = true;
        while (_input.hasNext()) {
            String toRead = _input.nextLine();
            if (!settingsRead && !toRead.startsWith("*")) {
                throw error("No configuration line in input");
            }
            if (toRead.startsWith("*")) {
                setUp(m, toRead);
                settingsRead = true;
            } else if (toRead.isEmpty()) {
                _output.println();
            } else {
                if (!firstRun) {
                    _output.println();
                }
                firstRun = false;
                printMessageLine(m.convert(toRead));
            }
        }
        boolean hadLine = false;
        while (_input.hasNextLine()) {
            hadLine = true;
            _output.println();
            _input.nextLine();
        }

        if (hadLine) {
            _output.println();
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphRange = _config.next();
            if (!Character.isLetter(alphRange.charAt(0))
                || (!Character.isLetter(alphRange.charAt(2))
                || (alphRange.charAt(1) != '-'))) {
                throw error("Need an alphabet");
            }
            _alphabet = new CharacterRange(alphRange.charAt(0),
                    alphRange.charAt(2));
            try {
                _numRotors = Integer.parseInt(_config.next());
            } catch (NumberFormatException e) {
                throw error("Need number of rotors");
            }
            try {
                _numPawls = Integer.parseInt(_config.next());
            } catch (NumberFormatException e) {
                throw error("Need number of pawls");
            }
            if (!_config.hasNext()) {
                throw error("need configuration file");
            }
            while (_config.hasNextLine()) {
                Rotor toAdd = readRotor();
                if (toAdd != null) {
                    _allRotors.add(toAdd);
                    Rotor prevRotor = _allRotors.get(_previousAddedRotor);
                    if (prevRotor.name().equals("tmp")) {
                        _allRotors.remove(_previousAddedRotor);
                    }
                    _previousAddedRotor += 1;
                }
            }


            return new Machine(_alphabet, _numRotors, _numPawls, _allRotors);

        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next().toUpperCase().trim();
            if (name.charAt(0) == '(') {
                String rest = _config.nextLine().trim();
                if (name.charAt(name.length() - 1) != ')') {
                    throw error("cycles must end in parenthesis");
                }
                Scanner restChecker = new Scanner(rest);
                while (restChecker.hasNext()) {
                    String scan = restChecker.next();
                    if (scan.charAt(scan.length() - 1) != ')') {
                        throw error("cycles must end in parenthesis");
                    }
                }
                Rotor rotorPrior = _allRotors.get(_previousAddedRotor - 1);
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
            if (!(typeNotch.charAt(0) == 'M'
                    || typeNotch.charAt(0) == 'N'
                    || typeNotch.charAt(0) == 'R')) {
                throw error("Wrong rotor types specified");
            }
            String cycles = _config.nextLine().trim();
            if (cycles.charAt(cycles.length() - 1) != ')') {
                throw error("cycles must end in parenthesis");
            }
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

    /** Returns number of pawls found in the input file PAWLSFROMSETTINGS.
     * Requires a list of indeces MOVINGROTORINDECES
     * of moving rotors from all rotors */
    private int getPawlsFromSettings(ArrayList<Integer> movingRotorIndeces) {

        int pawlsFromSettings = 0;
        for (int ind : movingRotorIndeces) {
            if (_allRotors.get(ind).rotates()) {
                pawlsFromSettings += 1;
            }
        }

        return pawlsFromSettings;

    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        Scanner myScan = new Scanner(settings);
        String ast = myScan.next().trim();
        String[] rotorsToSet = new String[M.numRotors()];
        ArrayList<String> allRotorsNames = new ArrayList<>();
        for (Rotor r : _allRotors) {
            allRotorsNames.add(r.name());
        }
        ArrayList<String> rotorsSoFar = new ArrayList<>();
        for (int i = 0; i < M.numRotors(); i += 1) {
            rotorsToSet[i] = myScan.next().trim();
            if (rotorsSoFar.contains(rotorsToSet[i])) {
                throw error("Duplicate rotor name");
            }
            rotorsSoFar.add(rotorsToSet[i]);
            if (!allRotorsNames.contains(rotorsToSet[i])) {
                throw error("Bad rotor name");
            }
        }
        String reflectorName = rotorsToSet[0];
        for (int i = 0; i < _allRotors.size(); i += 1) {
            String currReflector = _allRotors.get(i).name();
            if (currReflector.equals(reflectorName)) {
                if (!_allRotors.get(i).reflecting()) {
                    throw error("Reflector must come first");
                }
            }
        }
        ArrayList<Integer> movRotIndeces = new ArrayList<>();
        for (String r : rotorsToSet) {
            for (int j = 0; j < allRotorsNames.size(); j += 1) {
                if (allRotorsNames.get(j).equals(r)) {
                    movRotIndeces.add(j);
                }
            }
        }
        int pawlsFromSettings = getPawlsFromSettings(movRotIndeces);
        if (pawlsFromSettings > _numPawls) {
            throw error("Wrong number of arguments");
        }
        M.insertRotors(rotorsToSet);
        String rotorsSettings = myScan.next().trim();
        if (rotorsSettings.length() < (rotorsToSet.length - 1)) {
            throw error("Insufficient number of settings specified.");
        }
        for (int i = 0; i < rotorsSettings.length(); i += 1) {
            if (!Character.isLetter(rotorsSettings.charAt(i))) {
                throw error("Incorrect rotor setting type.");
            }
        }
        M.setRotors(rotorsSettings);
        if (myScan.hasNext()) {
            String rest = myScan.nextLine().trim();
            Permutation perm = new Permutation(rest, _alphabet);
            M.setPlugboard(perm);
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {

        int charsSoFar = 0;
        String result = "";
        for (int i = 0; i < msg.length(); i += 1) {
            char curr = msg.charAt(i);
            if (curr != ' ') {
                if ((charsSoFar % 5 == 0) && (charsSoFar > 0)) {
                    result = result +  " ";
                }
                result = result + curr;
                charsSoFar += 1;
            }
        }

        _output.print(result);

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
    /** All rotors from which rotors may be inserted into the machine. */
    private ArrayList<Rotor> _allRotors = new ArrayList<>();
    /** Number of rotors the machine will use at a given time. */
    private int _numRotors;
    /** Number of pawls to be used on the machine (aka moving rotors). */
    private int _numPawls;
    /** Keeps track of which rotor was added prior when adding rotors. */
    private int _previousAddedRotor = 0;


}
