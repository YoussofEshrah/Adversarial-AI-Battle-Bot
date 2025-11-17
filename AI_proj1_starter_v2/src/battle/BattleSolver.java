package battle;

public class BattleSolver {

    public Node initialNode; // this attribute MUST be used to store the initial node in the search tree
    private long nodesExpanded = 0L;

    // Instance variables related to visualization
    private boolean visualize = false;
    private int depth = 0;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_BG_WHITE = "\u001B[47m";

    public String solve(String initialStateString, boolean ab, boolean visualize) {
        String sol = "";
        this.visualize = visualize;

        ParsedState ps = parseInitialState(initialStateString);
        initialNode = new Node(ps.h0, ps.d0, ps.h1, ps.d1, ps.turn);

        if (visualize) {
            printHeader(ab);
            printInitialState(initialNode);
            System.out.println();
        }

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

        if (visualize) {
            printFinalSummary(result, ab);
        }

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

        if (visualize) {
            printNodeExploration(node, isMax, depth, "MINIMAX", Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        if (isTerminal(node)) {
            int util = utility(node);
            if (visualize) {
                printTerminalNode(node, util, depth);
            }
            return new SearchResult(util, "");
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
            String bestAction = "";
            for (String a : actions) {
                if (visualize) {
                    printActionConsidered(a, depth, isMax);
                }
                Node child = result(node, a);
                if (child == null)
                    continue; // should not happen with valid actions, but just in case it happens, we skip
                depth++;
                SearchResult r = minimax(child, false); // recursive call
                depth--;
                if (r.score > bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                    bestAction = a;
                }
                if (visualize) {
                    printActionResult(a, r.score, bestScore, depth, isMax);
                }
            }
            if (visualize) {
                printBestChoice(bestAction, bestScore, depth, isMax);
            }
            return new SearchResult(bestScore, bestPlan);
        } else {
            int bestScore = Integer.MAX_VALUE;
            String bestPlan = "";
            String bestAction = "";
            for (String a : actions) {
                if (visualize) {
                    printActionConsidered(a, depth, isMax);
                }
                Node child = result(node, a);
                if (child == null)
                    continue;
                depth++;
                SearchResult r = minimax(child, true);
                depth--;
                if (r.score < bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                    bestAction = a;
                }
                if (visualize) {
                    printActionResult(a, r.score, bestScore, depth, isMax);
                }
            }
            if (visualize) {
                printBestChoice(bestAction, bestScore, depth, isMax);
            }
            return new SearchResult(bestScore, bestPlan);
        }
    }

    // ========================= Step 8: Alpha-Beta =========================
    private SearchResult alphabeta(Node node, int alpha, int beta, boolean isMax) {
        // Count every node visit as an expansion
        countExpansion();

        if (visualize) {
            printNodeExploration(node, isMax, depth, "ALPHA-BETA", alpha, beta);
        }

        if (isTerminal(node)) {
            int util = utility(node);
            if (visualize) {
                printTerminalNode(node, util, depth);
            }
            return new SearchResult(util, "");
        }

        java.util.List<String> actions = generateActions(node);
        if (actions.isEmpty()) {
            return new SearchResult(utility(node), "");
        }

        if (isMax) {
            int bestScore = Integer.MIN_VALUE;
            String bestPlan = "";
            String bestAction = "";
            for (String a : actions) {
                if (visualize) {
                    printActionConsidered(a, depth, isMax);
                }
                Node child = result(node, a);
                if (child == null)
                    continue;
                depth++;
                SearchResult r = alphabeta(child, alpha, beta, false);
                depth--;
                if (r.score > bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                    bestAction = a;
                }
                if (visualize) {
                    printActionResult(a, r.score, bestScore, depth, isMax);
                }
                if (bestScore >= beta) {
                    // beta cut-off
                    if (visualize) {
                        printPruning("BETA", alpha, beta, bestScore, depth);
                    }
                    return new SearchResult(bestScore, bestPlan);
                }
                alpha = Math.max(alpha, bestScore);
                if (visualize) {
                    printAlphaBetaUpdate("ALPHA", alpha, beta, depth);
                }
            }
            if (visualize) {
                printBestChoice(bestAction, bestScore, depth, isMax);
            }
            return new SearchResult(bestScore, bestPlan);
        } else {
            int bestScore = Integer.MAX_VALUE;
            String bestPlan = "";
            String bestAction = "";
            for (String a : actions) {
                if (visualize) {
                    printActionConsidered(a, depth, isMax);
                }
                Node child = result(node, a);
                if (child == null)
                    continue;
                depth++;
                SearchResult r = alphabeta(child, alpha, beta, true);
                depth--;
                if (r.score < bestScore) {
                    bestScore = r.score;
                    bestPlan = a + (r.plan.isEmpty() ? "" : ("-" + r.plan));
                    bestAction = a;
                }
                if (visualize) {
                    printActionResult(a, r.score, bestScore, depth, isMax);
                }
                if (bestScore <= alpha) {
                    // alpha cut-off
                    if (visualize) {
                        printPruning("ALPHA", alpha, beta, bestScore, depth);
                    }
                    return new SearchResult(bestScore, bestPlan);
                }
                beta = Math.min(beta, bestScore);
                if (visualize) {
                    printAlphaBetaUpdate("BETA", alpha, beta, depth);
                }
            }
            if (visualize) {
                printBestChoice(bestAction, bestScore, depth, isMax);
            }
            return new SearchResult(bestScore, bestPlan);
        }
    }

    // ========================= Visualization Methods =========================

    private void printHeader(boolean ab) {
        System.out.println("\n" + ANSI_BOLD + ANSI_BG_WHITE + ANSI_BLUE +
                "===============================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "           BATTLE SOLVER VISUALIZATION" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_YELLOW + "           Algorithm: " +
                (ab ? "ALPHA-BETA PRUNING" : "MINIMAX") + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BG_WHITE + ANSI_BLUE +
                "===============================================================" + ANSI_RESET + "\n");
    }

    private void printInitialState(Node node) {
        System.out.println(ANSI_BOLD + ANSI_GREEN + "+==== INITIAL STATE ====+" + ANSI_RESET);
        printGameState(node);
        System.out.println(ANSI_BOLD + ANSI_GREEN + "+=======================+" + ANSI_RESET);
    }

    private void printGameState(Node node) {
        System.out.println(ANSI_BOLD + ANSI_GREEN + "  Team A: " + ANSI_RESET + formatTeam(node.h0, node.d0));
        System.out.println(ANSI_BOLD + ANSI_RED + "  Team B: " + ANSI_RESET + formatTeam(node.h1, node.d1));
        System.out.println(ANSI_BOLD + ANSI_YELLOW + "  Turn: " + ANSI_RESET +
                (node.turn == 'A' ? ANSI_GREEN + "Team A" : ANSI_RED + "Team B") + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_CYAN + "  Utility: " + ANSI_RESET + utility(node));
    }

    private String formatTeam(int[] health, int[] damage) {
        if (health == null || damage == null)
            return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < health.length; i++) {
            if (i > 0)
                sb.append(", ");
            String color = health[i] > 0 ? ANSI_WHITE : ANSI_PURPLE;
            sb.append(color).append("(H:").append(health[i])
                    .append(",D:").append(damage[i]).append(")").append(ANSI_RESET);
        }
        sb.append("]");
        return sb.toString();
    }

    private void printNodeExploration(Node node, boolean isMax, int depth, String algorithm, int alpha, int beta) {
        String indent = getIndent(depth);
        String playerColor = isMax ? ANSI_GREEN : ANSI_RED;
        String playerName = isMax ? "MAX (Team A)" : "MIN (Team B)";

        System.out.println();
        System.out
                .println(indent + ANSI_BOLD + ANSI_CYAN + "+-- Exploring Node (Depth " + depth + ") --+" + ANSI_RESET);
        System.out.println(indent + ANSI_BOLD + playerColor + "| Player: " + playerName + ANSI_RESET);

        if (algorithm.equals("ALPHA-BETA")) {
            System.out.println(indent + ANSI_BOLD + ANSI_YELLOW + "| a=" + formatValue(alpha) +
                    ", b=" + formatValue(beta) + ANSI_RESET);
        }

        if (node.actionFromParent != null) {
            System.out.println(indent + ANSI_BOLD + ANSI_PURPLE + "| From Action: " +
                    formatAction(node.actionFromParent) + ANSI_RESET);
        }
        System.out.println(indent + ANSI_BOLD + ANSI_CYAN + "+------------------------------+" + ANSI_RESET);
    }

    private void printActionConsidered(String action, int depth, boolean isMax) {
        String indent = getIndent(depth + 1);
        String color = isMax ? ANSI_GREEN : ANSI_RED;
        System.out.println(indent + color + "> Trying action: " + formatAction(action) + ANSI_RESET);
    }

    private void printActionResult(String action, int score, int currentBest, int depth, boolean isMax) {
        String indent = getIndent(depth + 1);
        boolean isBetter = isMax ? (score > currentBest || currentBest == Integer.MIN_VALUE)
                : (score < currentBest || currentBest == Integer.MAX_VALUE);
        String resultColor = isBetter ? ANSI_YELLOW : ANSI_WHITE;
        String symbol = isBetter ? "*" : "o";
        System.out.println(indent + resultColor + "  " + symbol + " Result: " + score +
                " (Current best: " + formatValue(currentBest) + ")" + ANSI_RESET);
    }

    private void printBestChoice(String action, int score, int depth, boolean isMax) {
        if (action.isEmpty())
            return;
        String indent = getIndent(depth);
        String color = isMax ? ANSI_GREEN : ANSI_RED;
        System.out.println(indent + ANSI_BOLD + color + "[BEST] Move: " + formatAction(action) +
                " -> Score: " + score + ANSI_RESET);
    }

    private void printTerminalNode(Node node, int utility, int depth) {
        String indent = getIndent(depth);
        System.out.println(indent + ANSI_BOLD + ANSI_PURPLE + "[!] TERMINAL NODE" + ANSI_RESET);
        System.out.println(indent + ANSI_BOLD + ANSI_CYAN + "  Utility: " + utility + ANSI_RESET);
        System.out.println(indent + ANSI_PURPLE + "  A Health: " + sum(node.h0) +
                ", B Health: " + sum(node.h1) + ANSI_RESET);
    }

    private void printPruning(String cutoffType, int alpha, int beta, int value, int depth) {
        String indent = getIndent(depth + 1);
        System.out.println(indent + ANSI_BOLD + ANSI_RED + "[X] " + cutoffType + " CUT-OFF! " + ANSI_RESET);
        System.out.println(indent + ANSI_RED + "  Pruned at value " + value +
                " (a=" + formatValue(alpha) + ", b=" + formatValue(beta) + ")" + ANSI_RESET);
    }

    private void printAlphaBetaUpdate(String paramName, int alpha, int beta, int depth) {
        String indent = getIndent(depth + 1);
        System.out.println(indent + ANSI_YELLOW + "  Updated " + paramName + ": a=" +
                formatValue(alpha) + ", b=" + formatValue(beta) + ANSI_RESET);
    }

    private void printFinalSummary(SearchResult result, boolean ab) {
        System.out.println("\n" + ANSI_BOLD + ANSI_BG_WHITE + ANSI_BLUE +
                "===============================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_GREEN + "           FINAL RESULT" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BG_WHITE + ANSI_BLUE +
                "===============================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_YELLOW + "  Optimal Plan: " + ANSI_RESET +
                ANSI_CYAN + formatPlan(result.plan) + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_YELLOW + "  Final Score: " + ANSI_RESET +
                ANSI_GREEN + result.score + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_YELLOW + "  Nodes Expanded: " + ANSI_RESET +
                ANSI_PURPLE + getNodesExpanded() + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BG_WHITE + ANSI_BLUE +
                "===============================================================" + ANSI_RESET + "\n");
    }

    private String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  | ");
        }
        return sb.toString();
    }

    private String formatAction(String action) {
        if (action == null || action.isEmpty())
            return "";
        char team = action.charAt(0);
        String color = team == 'A' ? ANSI_GREEN : ANSI_RED;
        return color + ANSI_BOLD + action + ANSI_RESET;
    }

    private String formatPlan(String plan) {
        if (plan == null || plan.isEmpty())
            return "(no moves)";
        String[] moves = plan.split("-");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.length; i++) {
            if (i > 0)
                sb.append(" -> ");
            sb.append(formatAction(moves[i]));
        }
        return sb.toString();
    }

    private String formatValue(int value) {
        if (value == Integer.MIN_VALUE)
            return "-INF";
        if (value == Integer.MAX_VALUE)
            return "+INF";
        return String.valueOf(value);
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
    // Actions are sorted to favor highest damage attackers targeting lowest health
    // defenders.
    public static java.util.List<String> generateActions(Node node) {
        java.util.List<String> actions = new java.util.ArrayList<>();
        if (node == null)
            return actions;
        if (node.turn == 'A') {
            if (node.h0 != null && node.h1 != null && node.d0 != null) { // h0 and h1 are the health arrays of both
                                                                         // teams
                for (int i = 0; i < node.h0.length; i++) {
                    if (node.h0[i] > 0) {
                        for (int j = 0; j < node.h1.length; j++) {
                            if (node.h1[j] > 0) {
                                actions.add("A(" + i + "," + j + ")");
                            }
                        }
                    }
                }
                // Sort actions: prioritize high damage attackers (descending) and low health
                // targets (ascending)
                actions.sort((a1, a2) -> {
                    int attacker1 = extractAttackerIndex(a1);
                    int target1 = extractTargetIndex(a1);
                    int attacker2 = extractAttackerIndex(a2);
                    int target2 = extractTargetIndex(a2);

                    // First, compare by attacker damage (higher damage first)
                    int dmgCompare = Integer.compare(node.d0[attacker2], node.d0[attacker1]);
                    if (dmgCompare != 0)
                        return dmgCompare;

                    // Then, compare by target health (lower health first)
                    return Integer.compare(node.h1[target1], node.h1[target2]);
                });
            }
        } else if (node.turn == 'B') {
            if (node.h1 != null && node.h0 != null && node.d1 != null) {
                for (int i = 0; i < node.h1.length; i++) {
                    if (node.h1[i] > 0) {
                        for (int j = 0; j < node.h0.length; j++) {
                            if (node.h0[j] > 0) {
                                actions.add("B(" + i + "," + j + ")");
                            }
                        }
                    }
                }
                // Sort actions: prioritize high damage attackers (descending) and low health
                // targets (ascending)
                actions.sort((a1, a2) -> {
                    int attacker1 = extractAttackerIndex(a1);
                    int target1 = extractTargetIndex(a1);
                    int attacker2 = extractAttackerIndex(a2);
                    int target2 = extractTargetIndex(a2);

                    // First, compare by attacker damage (higher damage first)
                    int dmgCompare = Integer.compare(node.d1[attacker2], node.d1[attacker1]);
                    if (dmgCompare != 0)
                        return dmgCompare;

                    // Then, compare by target health (lower health first)
                    return Integer.compare(node.h0[target1], node.h0[target2]);
                });
            }
        }
        return actions; // example return will look like: ["A(0,0)", "A(0,1)", ...] - unit 0 of team A
                        // can attack unit 0 or 1 of team B and so on
    }

    // Helper method to extract attacker index from action string "X(i,j)"
    private static int extractAttackerIndex(String action) {
        int l = action.indexOf('(');
        int comma = action.indexOf(',');
        return Integer.parseInt(action.substring(l + 1, comma));
    }

    // Helper method to extract target index from action string "X(i,j)"
    private static int extractTargetIndex(String action) {
        int comma = action.indexOf(',');
        int r = action.indexOf(')');
        return Integer.parseInt(action.substring(comma + 1, r));
    }

}