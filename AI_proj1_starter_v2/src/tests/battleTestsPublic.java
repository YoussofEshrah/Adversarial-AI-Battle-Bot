package tests;

import battle.BattleSolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;

import java.time.Duration;
public class battleTestsPublic {

    @Test
    public void test_plan_a() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "5,3;4,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);
            int expectedScore = 12345;

            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, false);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });
    }





    //--------------------------minimax only tests----------------------------------------
    @Test
    public void test_minimax_a() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "5,3;4,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);

            int expectedScore = 4;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });
    }



    @Test
    public void test_minimax_c() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "1,1,2,3,6,7;5,5,3,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);

            int expectedScore = 8;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });
    }

    @Test
    public void test_minimax_d() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "1,1,2,3,6,7;5,5,3,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);

            int expectedScore = 8;
            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });
    }

    @Test
    public void test_minimax_e() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {


            String initialState = "7,3,11,9;2,8,4,10,1,6,5,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);

            int expectedScore = 3;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });
    }




    @Test
    public void test_minimax_g() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {
            String initialState = "1,1,2,3,6,7;5,5,10,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);

            int expectedScore = 7;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);
        });

    }




    @Test
    public void test_minimax_i() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {
            String initialState = "1,10,5,2,3,5;6,7,3,1,13,4;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, false, false);
            int expectedScore = -4;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });
    }





//-------------------------------alphabeta tests-----------------------------------------------------


    @Test
    public void test_alphabeta_a() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "5,3;4,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, true, false);

            int expectedScore = 4;

            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });

    }




    @Test
    public void test_alphabeta_c() {

        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {


            String initialState = "1,1,2,3,6,7;5,5,3,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, true, false);

            int expectedScore = 8;
            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);
        });

    }





    @Test
    public void test_alphabeta_e() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "7,3,11,9;2,8,4,10,1,6,5,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, true, false);

            int expectedScore = 3;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);
        });

    }




    @Test
    public void test_alphabeta_g() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "11,2,12,3,9,8,13,14;1,4,6,10,7,9,5,2;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, true, false);

            int expectedScore = 30;


            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);
        });

    }





    @Test
    public void test_alphabeta_i() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "1,10,5,2,3,5;6,7,3,1,13,4;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, true, false);

            int expectedScore = -4;

            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);

        });

    }

    @Test
    public void test_alphabeta_j() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String initialState = "1,1,1,1,2,3,6,7;5,5,10,1;A;";
            BattleSolver b = new BattleSolver();
            String sol = b.solve(initialState, true, false);

            int expectedScore = 8;

            BattleGameChecker.ValidationResult validation = BattleGameChecker.validateSolution(initialState, sol, expectedScore, true);
            assertTrue(validation.isValid, "Valid solution should pass validation: " + validation.errorMessage);
        });

    }

    //-------------------------------alpha beta vs minimax tests-----------------------------------------
    @Test
    public void test1_ndoesExpanded() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(100), () -> {

            String [] initialStrings = {"5,3;4,1;A;",
                    "1,1,2,3,6,7;5,5,3,1;A;",
                    "7,3,11,9;2,8,4,10,1,6,5,1;A;",
                    "1,10,5,2,3,5;6,7,3,1,13,4;A;",

            };

            ArrayList<Integer> nodesExpanded_minimax = new ArrayList<Integer>();
            ArrayList<Integer> nodesExpanded_alphabeta = new ArrayList<Integer>();
            boolean success = false;

            for (String init_str :
                    initialStrings) {
                BattleSolver b1 = new BattleSolver();
                String sol1 = b1.solve(init_str, false, false);
                long nodesExpanded1 = Long.parseLong(sol1.split(";")[2]);

                BattleSolver b2 = new BattleSolver();
                String sol2 = b2.solve(init_str, true, false);
                long nodesExpanded2 = Long.parseLong(sol2.split(";")[2]);


                if (nodesExpanded1>nodesExpanded2) {
                    success = true;
                    break;
                }
                if (nodesExpanded1<nodesExpanded2) {
                    success = false;
                    break;
                }
            }



            assertTrue(success, "Alpha-Beta pruning not reducing nodes expanded");
        });
    }


}