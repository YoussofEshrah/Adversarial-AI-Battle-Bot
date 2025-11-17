package battle;

public class TestVisualization {
    public static void main(String[] args) {
        BattleSolver solver = new BattleSolver();

        // Simple test case: 2v2 battle
        // String testState = "10,5,8,3;12,4,6,2;A;";
        String testState = "1,1,1,1;1,1,1,1;A;";

        System.out.println("=".repeat(70));
        System.out.println("MINIMAX VISUALIZATION TEST");
        System.out.println("=".repeat(70));

        // Test with Minimax and visualization
        String result1 = solver.solve(testState, false, true);
        System.out.println("\n\nResult: " + result1);

        System.out.println("\n\n");
        System.out.println("=".repeat(70));
        System.out.println("ALPHA-BETA VISUALIZATION TEST");
        System.out.println("=".repeat(70));

        // Test with Alpha-Beta and visualization
        BattleSolver solver2 = new BattleSolver();
        String result2 = solver2.solve(testState, true, true);
        System.out.println("\n\nResult: " + result2);

        System.out.println("\n\n");
        System.out.println("=".repeat(70));
        System.out.println("COMPARISON (No Visualization)");
        System.out.println("=".repeat(70));

        // Compare without visualization
        BattleSolver solver3 = new BattleSolver();
        String result3 = solver3.solve(testState, false, false);
        System.out.println("Minimax (no viz): " + result3);

        BattleSolver solver4 = new BattleSolver();
        String result4 = solver4.solve(testState, true, false);
        System.out.println("Alpha-Beta (no viz): " + result4);
    }
}
