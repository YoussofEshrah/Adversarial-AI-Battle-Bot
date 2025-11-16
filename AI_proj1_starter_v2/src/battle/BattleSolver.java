package battle;

public class BattleSolver {

    public Node initialNode; // this attribute MUST be used to store the initial node in the search tree

    public String solve(String initialStateString, boolean ab, boolean visualize) {
        // TODO: implement this function

        String sol = "";
        return sol;

    }

    // ========================= Step 1: Input Parser =========================
    // Parses initialStateString of the form
    // "h0_0,d0_0,h0_1,d0_1,...;h1_0,d1_0,h1_1,d1_1,...;T;"
    // where T is either 'A' or 'B'.
    private static class ParsedState {
        int[] h0;
        int[] d0;
        int[] h1;
        int[] d1;
        char turn;
    }

    private static ParsedState parseInitialState(String initialStateString) {
        if (initialStateString == null) {
            throw new IllegalArgumentException("initialStateString is null");
        }

        String s = initialStateString.trim();
        // Split by ';' and ignore trailing empty parts if any
        String[] parts = s.split(";");

        // Expect at least 3 meaningful segments: A-list;B-list;Turn
        // Some inputs may include a trailing ';' resulting in an extra empty segment.
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid state format: expected at least 3 segments separated by ';'");
        }

        String aList = parts[0].trim();
        String bList = parts[1].trim();
        String turnStr = parts[2].trim();

        if (turnStr.isEmpty()) {
            // If turn ended up in the 3rd index empty due to double ';;', try next part if
            // available
            if (parts.length > 3) {
                turnStr = parts[3].trim();
            }
        }

        if (turnStr.isEmpty()) {
            throw new IllegalArgumentException("Missing turn indicator ('A' or 'B') in state string");
        }

        char turn = turnStr.charAt(0);
        if (turn != 'A' && turn != 'B') {
            throw new IllegalArgumentException("Invalid turn indicator: " + turn);
        }

        int[] aValues = parseCommaSeparatedInts(aList);
        int[] bValues = parseCommaSeparatedInts(bList);

        if (aValues.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Team A list must contain even number of integers (health,damage pairs)");
        }
        if (bValues.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Team B list must contain even number of integers (health,damage pairs)");
        }

        int aUnits = aValues.length / 2;
        int bUnits = bValues.length / 2;

        int[] h0 = new int[aUnits];
        int[] d0 = new int[aUnits];
        int[] h1 = new int[bUnits];
        int[] d1 = new int[bUnits];

        for (int i = 0, u = 0; i < aValues.length; i += 2, u++) {
            int h = aValues[i];
            int d = aValues[i + 1];
            validateNonNegative(h, "A health at index " + u);
            validateNonNegative(d, "A damage at index " + u);
            h0[u] = h;
            d0[u] = d;
        }

        for (int i = 0, u = 0; i < bValues.length; i += 2, u++) {
            int h = bValues[i];
            int d = bValues[i + 1];
            validateNonNegative(h, "B health at index " + u);
            validateNonNegative(d, "B damage at index " + u);
            h1[u] = h;
            d1[u] = d;
        }

        ParsedState ps = new ParsedState();
        ps.h0 = h0;
        ps.d0 = d0;
        ps.h1 = h1;
        ps.d1 = d1;
        ps.turn = turn;
        return ps;
    }

    private static int[] parseCommaSeparatedInts(String list) {
        if (list.isEmpty()) {
            return new int[0];
        }
        String[] tokens = list.split(",");
        int[] res = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            String t = tokens[i].trim();
            if (t.isEmpty()) {
                throw new IllegalArgumentException("Empty number token at position " + i);
            }
            try {
                res[i] = Integer.parseInt(t);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid integer: '" + t + "'", nfe);
            }
        }
        return res;
    }

    private static void validateNonNegative(int v, String what) {
        if (v < 0) {
            throw new IllegalArgumentException(what + " must be non-negative, got " + v);
        }
    }

}