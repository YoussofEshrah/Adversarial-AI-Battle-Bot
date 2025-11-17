package battle;

public class BattleSolver {

    public Node initialNode; // this attribute MUST be used to store the initial node in the search tree
    private long nodesExpanded = 0L;

    public String solve(String initialStateString, boolean ab, boolean visualize) {
        String sol = "";

        // Minimax implementation
        ParsedState ps = parseInitialState(initialStateString);
        initialNode = new Node(ps.h0, ps.d0, ps.h1, ps.d1, ps.turn);

        resetNodesExpanded();
        SearchResult result;

        if (ab) {
            result = alphabeta(initialNode, Integer.MIN_VALUE, Integer.MAX_VALUE, ps.turn == 'A');

        } else {
            result = minimax(initialNode, ps.turn == 'A');
        }

        String plan = result.plan.replaceAll("-", ",");
        String score = Integer.toString(result.score);
        String nodesExp = Long.toString(getNodesExpanded());

        sol = plan + ";" + score + ";" + nodesExp;
        return sol;

    }

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

    // Terminal when either team's total health reaches zero.
    private static boolean isTerminal(Node node) {
        if (node == null)
            return true; // defensive: treat null as terminal
        int sumA = sum(node.h0);
        int sumB = sum(node.h1);
        return sumA == 0 || sumB == 0;
    }

    // Utility defined as sum(A health) - sum(B health). Used at terminal states
    // (fakart en momken law pieces 3andaha
    // dmg aktar yeb2a leeha weight aktar ka utility bas dah mesh implied men el
    // description awy)
    private static int utility(Node node) {
        if (node == null)
            return 0;
        return sum(node.h0) - sum(node.h1);
    }

    private static int sum(int[] a) {
        if (a == null)
            return 0;
        int s = 0;
        for (int v : a)
            s += v;
        return s;
    }

    private void resetNodesExpanded() {
        nodesExpanded = 0L;
    }

    private void countExpansion() {
        nodesExpanded++;
    }

    private long getNodesExpanded() {
        return nodesExpanded;
    }

    private static class SearchResult {
        final int score;
        final String plan; // hyphen-separated action sequence

        SearchResult(int score, String plan) {
            this.score = score;
            this.plan = plan == null ? "" : plan;
        }
    }

    // Minimax with plan reconstruction. isMax should be true when it's A's turn.
    private SearchResult minimax(Node node, boolean isMax) {
        // Count every node visit as an expansion
        countExpansion();

        if (isTerminal(node)) {
            return new SearchResult(utility(node), "");
        }

        java.util.List<String> actions = generateActions(node);
        if (actions.isEmpty()) {
            // Defensive fallback; ideally terminal would have been true/ possible in
            // stalemates like in chess if they exist
            return new SearchResult(utility(node), "");
        }

        if (isMax) {
            int bestScore = Integer.MIN_VALUE;
            String bestPlan = "";
            for (String a : actions) {
                Node child = result(node, a);
                if (child == null)
                    continue; // should not happen with valid actions, but just in case it happens, we skip
                SearchResult r = minimax(child, false); // recursive call
                if (r.score > bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                }
            }
            return new SearchResult(bestScore, bestPlan);
        } else {
            int bestScore = Integer.MAX_VALUE;
            String bestPlan = "";
            for (String a : actions) {
                Node child = result(node, a);
                if (child == null)
                    continue;
                SearchResult r = minimax(child, true);
                if (r.score < bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                }
            }
            return new SearchResult(bestScore, bestPlan);
        }
    }

    // ========================= Step 8: Alpha-Beta =========================
    private SearchResult alphabeta(Node node, int alpha, int beta, boolean isMax) {
        // Count every node visit as an expansion
        countExpansion();

        if (isTerminal(node)) {
            return new SearchResult(utility(node), "");
        }

        java.util.List<String> actions = generateActions(node);
        if (actions.isEmpty()) {
            return new SearchResult(utility(node), "");
        }

        if (isMax) {
            int bestScore = Integer.MIN_VALUE;
            String bestPlan = "";
            for (String a : actions) {
                Node child = result(node, a);
                if (child == null)
                    continue;
                SearchResult r = alphabeta(child, alpha, beta, false);
                if (r.score > bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                }
                if (bestScore >= beta) {
                    // beta cut-off
                    return new SearchResult(bestScore, bestPlan);
                }
                alpha = Math.max(alpha, bestScore);
            }
            return new SearchResult(bestScore, bestPlan);
        } else {
            int bestScore = Integer.MAX_VALUE;
            String bestPlan = "";
            for (String a : actions) {
                Node child = result(node, a);
                if (child == null)
                    continue;
                SearchResult r = alphabeta(child, alpha, beta, true);
                if (r.score < bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                }
                if (bestScore <= alpha) {
                    // alpha cut-off
                    return new SearchResult(bestScore, bestPlan);
                }
                beta = Math.min(beta, bestScore);
            }
            return new SearchResult(bestScore, bestPlan);
        }
    }

    // Applies an action string to a node and returns a new child node with updated
    // state.
    // Returns null if the action is invalid for the given node.
    public static Node result(Node node, String action) { // action format: "A(i,j)" or "B(i,j)", Node is the current
                                                          // state
        if (node == null || action == null || action.length() < 5)
            return null; // minimal len: X(0,0)

        // Create a deep-copied child state
        Node child = new Node(node);

        char actor = action.charAt(0);
        // Parse indices inside parentheses
        int l = action.indexOf('(');
        int r = action.lastIndexOf(')'); // beygeeb el index of the last occurunce of ')'
        if (l < 0 || r < 0 || r <= l + 1)
            return null;
        String inside = action.substring(l + 1, r);
        String[] parts = inside.split(",");
        if (parts.length != 2)
            return null;
        int attackerIdx;
        int targetIdx;
        try {
            attackerIdx = Integer.parseInt(parts[0].trim());
            targetIdx = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException nfe) {
            return null;
        }

        if (actor == 'A') {
            // Validate turn and indices and alive statuses
            if (child.turn != 'A' || child.h0 == null || child.h1 == null) // check if it's A's turn and arrays are not
                                                                           // null
                return null;
            if (attackerIdx < 0 || attackerIdx >= child.h0.length) // check if attacker index is valid
                return null;
            if (targetIdx < 0 || targetIdx >= child.h1.length) // check if target index is valid
                return null;
            if (child.h0[attackerIdx] <= 0 || child.h1[targetIdx] <= 0) // check if both attacker and target are alive
                return null;

            int dmg = (child.d0 != null && attackerIdx < child.d0.length) ? child.d0[attackerIdx] : 0;
            int newHealth = child.h1[targetIdx] - dmg;
            child.h1[targetIdx] = Math.max(0, newHealth); // health cannot go below 0
            child.turn = 'B';
        } else if (actor == 'B') {
            if (child.turn != 'B' || child.h1 == null || child.h0 == null)
                return null;
            if (attackerIdx < 0 || attackerIdx >= child.h1.length)
                return null;
            if (targetIdx < 0 || targetIdx >= child.h0.length)
                return null;
            if (child.h1[attackerIdx] <= 0 || child.h0[targetIdx] <= 0)
                return null;

            int dmg = (child.d1 != null && attackerIdx < child.d1.length) ? child.d1[attackerIdx] : 0;
            int newHealth = child.h0[targetIdx] - dmg;
            child.h0[targetIdx] = Math.max(0, newHealth);
            child.turn = 'A';
        } else {
            return null;
        }

        child.parent = node;
        child.actionFromParent = action;
        return child;
    }

    // Returns all legal action strings for the given node in order.
    // Format: "A(i,j)" if node.turn=='A', else "B(i,j)".
    // Only include attackers with health>0 and targets with health>0.
    public static java.util.List<String> generateActions(Node node) {
        java.util.List<String> actions = new java.util.ArrayList<>();
        if (node == null)
            return actions;
        if (node.turn == 'A') {
            if (node.h0 != null && node.h1 != null) { // h0 and h1 are the health arrays of both teams
                for (int i = 0; i < node.h0.length; i++) {
                    if (node.h0[i] > 0) {
                        for (int j = 0; j < node.h1.length; j++) {
                            if (node.h1[j] > 0) {
                                actions.add("A(" + i + "," + j + ")");
                            }
                        }
                    }
                }
            }
        } else if (node.turn == 'B') {
            if (node.h1 != null && node.h0 != null) {
                for (int i = 0; i < node.h1.length; i++) {
                    if (node.h1[i] > 0) {
                        for (int j = 0; j < node.h0.length; j++) {
                            if (node.h0[j] > 0) {
                                actions.add("B(" + i + "," + j + ")");
                            }
                        }
                    }
                }
            }
        }
        return actions; // example return will look like: ["A(0,0)", "A(0,1)", ...] - unit 0 of team A
                        // can attack unit 0 or 1 of team B and so on
    }

}