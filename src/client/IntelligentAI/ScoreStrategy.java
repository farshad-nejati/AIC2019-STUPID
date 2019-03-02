package client.IntelligentAI;

import client.model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoreStrategy {
    public static Integer distanceToZone(Hero myHero, MyDirection direction, World virtualWorld, ArrayList<Move> copyOfMyHeroesMove, ArrayList<Cell> blockCells) {
        Integer score = 0;
        Move move = Move.findByHero(copyOfMyHeroesMove, myHero);
        Integer index = copyOfMyHeroesMove.indexOf(move);
        Cell myherocell = move.getNextCell(); //TODO: this must be updated of my hero cell
        if (myherocell.isInObjectiveZone()) {
            score += Score.IN_ZONE;
        }
        if (move.getTargetZoneCell() != null) {
            Cell selectedObjectiveCell = move.getTargetZoneCell();
            //Todo:above code must be replace with nearest objective zone cell

            Direction[] distancepath = virtualWorld.getPathMoveDirections(myherocell, selectedObjectiveCell, blockCells);
            //TODO: find distacePath based on block cell
            Integer distanceLenghtCost = Score.DISTANCE_COST * (distancepath.length);
            score += distanceLenghtCost;
//            System.out.println("distanceLenghtCost = " + distanceLenghtCost);
        }
        if (!direction.equals(MyDirection.FIX)) {
            score += Score.MOVE_COST;
        }
//        System.out.println("distance zone Score= " + score);
        return score;
    }

    public static Integer otherMyHeroCell(Hero myHero, MyDirection direction, Hero oppHero, World virtualWorld, ArrayList<Move> copyOfMyHeroesMove) {
        Integer score = 0;
        Cell myHeroCurrentCell = null;
        Cell myHeroNextCell = null;
        for (Move myHeroMove : copyOfMyHeroesMove) {
            if (myHeroMove.getHero().equals(myHero)) {
                myHeroCurrentCell = myHeroMove.getCurrentCell();
                myHeroNextCell = myHeroMove.getNextCell();
                break;
            }
        }
//        if (virtualWorld.getCurrentTurn() == 10) {
//            int p = 0;
//        }
        boolean flag = false;
        for (Move myOtherHeroMove : copyOfMyHeroesMove) {
            boolean checkMyHero = myOtherHeroMove.getHero().equals(myHero);
            boolean checkTwoHero = myOtherHeroMove.getCurrentCell().equals(myHeroNextCell);
            if (checkTwoHero && !(checkMyHero)) {
                score += Score.MY_HERO_CELL;
                flag = true;
//                if (myHero.getCurrentCell().equals(myHeroNextCell)){
//                    score += Score.MY_OTHER_HERO_AROUND_NEGATIVE_COST;
//                }
                break;
            }
        }

        HashMap<Cell, Direction> cellNeighbors = Utility.getCellNeighbors(myHeroCurrentCell, virtualWorld.getMap());
        MyDirection directionCheck = Utility.getDirectionFromCells(myHeroCurrentCell, myHeroNextCell);


        for (Cell cellNeighbor : cellNeighbors.keySet()) {

            if (!copyOfMyHeroesMove.isEmpty()) {
                Integer i = 0;
                HashMap<Cell, Direction> cellNeighborsNeighbor = Utility.getCellNeighbors(cellNeighbor, virtualWorld.getMap());
                for (Cell cellNeighborNeighbor : cellNeighborsNeighbor.keySet()) {
                    for (Move heroMoveneighbor : copyOfMyHeroesMove) {
                        Hero otherOurHeroneighbor = heroMoveneighbor.getHero();
                        if (otherOurHeroneighbor.getCurrentCell().equals(cellNeighborNeighbor)) {
                            i++;
                        }
                    }
                }
                score += (i * Score.MY_OTHER_HERO_AROUND_NEGATIVE_COST_IN_ZONE);

                if (cellNeighbor.isInObjectiveZone()) {
                    score += Score.IN_ZONE;
                }

                if (directionCheck.equals(MyDirection.FIX)) {
//                    score += Score.MY_OTHER_HERO_AROUND_NEGATIVE_COST_IN_ZONE;
                }


//                    if (otherOurHero.getCurrentCell().equals(cellNeighbor)) {
//                        Integer j = 0;
//                        for (Move ourheroMove: copyOfMyHeroesMove){
//                            Hero ourhero = ourheroMove.getHero();
//                            if (!ourhero.equals(myHero) && ourhero.getCurrentCell().equals(myHeroNextCell)){
//                                j++;
//                            }
//                        }
//                            if (myHeroCurrentCell.isInObjectiveZone()){
//                                score += (i * Score.MY_OTHER_HERO_AROUND_NEGATIVE_COST_IN_ZONE);
//
//                            }else {
//                                score += Score.MY_OTHER_HERO_AROUND_NEGATIVE_COST;
//
//                            }
//                    }

            }

        }


//        if (flag && directionCheck.equals(MyDirection.FIX)){
//            score += Score.MY_OTHER_HERO_AROUND_NEGATIVE_COST;
//        }
//        System.out.println("other hero cell score= " + score);
        return score;
    }

    public static Integer otherWallCell(Hero myHero, MyDirection direction, World virtualWorld, ArrayList<Move> copyOfMyHeroesMove) {
        Integer score = 0;
        Cell myHeroNextCell = null;
        for (Move myHeroMove : copyOfMyHeroesMove) {
            if (myHeroMove.getHero().equals(myHero)) {
                myHeroNextCell = myHeroMove.getNextCell();
            }
        }
        if (myHeroNextCell.isWall()) {
            score += Score.WALL_SCORE;
        }
//        System.out.println("wall cell score= " + score);

        return score;
    }

    public static Integer hitByOppHeroes(Hero myHero, MyDirection direction, World virtualWorld, ArrayList<Move> copyOfMyHeroesMove, ArrayList<Move> copyOfOppHeroesMove) {

        Integer losingHealthSum = 0;
        Integer killDistanceSum = 0;
        Integer canHitSum = 0;

        Integer beforeCellScore = 0;
        Integer losingHealthScore = 0;
        Integer killDistanceScore = 0;
        Integer canHitScore = 0;

        Move move = Move.findByHero(copyOfMyHeroesMove, myHero);
        Cell myHeroCell2 = move.getNextCell(); //TODO: this must be updated of my hero cell
//        System.out.println("myHeroCell2 = " +myHeroCell2.getRow() + " "+ myHeroCell2.getColumn());
        Cell myHeroCell = Utility.getCellFromDirection(move.getCurrentCell(), direction, virtualWorld.getMap());
//        System.out.println("myHeroCell = " + myHeroCell.getRow() + " "+ myHeroCell.getColumn());
        for (Move oppHeroMove : copyOfOppHeroesMove) {
            boolean canHit = false;
            Hero hero = oppHeroMove.getHero();
//            Cell oppHeroCell = oppHeroMove.getNextCell();
            Cell oppHeroCell = oppHeroMove.getNextCell();
            if (oppHeroCell.isInVision()) {
                Ability maxLosingHealthAbility = null;
                int maxLosingHealth = 0;
                int distance = virtualWorld.manhattanDistance(myHeroCell, oppHeroCell);
                for (Ability ability : hero.getOffensiveAbilities()) {
                    if (maxLosingHealthAbility == null) {
                        maxLosingHealthAbility = ability;
                    }


                    boolean abilityCanHit = distance <= (maxLosingHealthAbility.getRange() + maxLosingHealthAbility.getAreaOfEffect());

                    if (abilityCanHit) {
                        canHit = true;
                        if (ability.getPower() > maxLosingHealth) {
                            maxLosingHealthAbility = ability;
                            maxLosingHealth = ability.getPower();
                        }
                    }
                }
                if (canHit) {
                    canHitSum++;
                    killDistanceSum += distance;

                }
                if (maxLosingHealthAbility != null) {
                    losingHealthSum += maxLosingHealth;
                }
            }
        }

        canHitScore = canHitSum * Score.CAN_HIT_COST;

        if (losingHealthSum > myHero.getCurrentHP()) {
            losingHealthScore = Score.KILL_COST;
            if (direction.equals(MyDirection.FIX)) {
                losingHealthScore += 2 * Score.MOVE_COST;
            }
            killDistanceScore = killDistanceSum * Score.KILL_DISTANCE_COST;

            Cell moveCell = myHeroCell;
            Cell beforeCell = move.getBeforeCell();
            if (moveCell.equals(beforeCell)) {
                beforeCellScore = Score.BEFORE_CELL_SCORE;
            }
        } else {
            losingHealthScore = Score.HEALTH_COST * losingHealthSum;
        }
//        System.out.println("losingHealthScore = " + losingHealthScore);
//        System.out.println("k = " + killDistanceScore);
//        System.out.println("canHitScore = " + canHitScore);

        return losingHealthScore + killDistanceScore + canHitScore + beforeCellScore;
//        return losingHealthScore ;
    }

    public static Integer reduceDistanceWithOppHeroesInObjectiveZone(Hero myHero, MyDirection myHeroDirection, World virtualWorld, ArrayList<Move> copyOfMyHeroesMove, ArrayList<Move> copyOfOppHeroesMove) {

        Integer score = 0;
        Integer distanceSum = 0;
        Integer distanceScore = 0;

        Integer myHeroCanHitMaxDistance = 0;
        boolean myHeroCanHitAnyone = false;
        boolean canNextCellAttack = false;
        Move oppHeroMoveWithMinimumHealth = null;

        Move move = Move.findByHero(copyOfMyHeroesMove, myHero);
        Cell myHeroMoveNextCell = move.getNextCell();
        Cell myHeroMoveCurrentCell = myHero.getCurrentCell();

        if (myHeroMoveCurrentCell.isInObjectiveZone()) {

            for (Ability myHeroAbility : myHero.getOffensiveAbilities()) {
                int canHitDistance = myHeroAbility.getRange() + myHeroAbility.getAreaOfEffect();
                if (myHeroAbility.isReady() && (canHitDistance > myHeroCanHitMaxDistance)) {
//                    System.out.println("Hero: " + myHero.getId() + " myHeroAbility = " + myHeroAbility.getName());
                    myHeroCanHitMaxDistance = canHitDistance;
                }
            }

            if (virtualWorld.getCurrentTurn()>=24) {
                int i = 0;
            }

            for (Move oppHeroMove : copyOfOppHeroesMove) {

                Hero oppHeroMoveHero = oppHeroMove.getHero();
                Cell oppHeroMoveNextCell = oppHeroMove.getNextCell();
//                Cell oppHeroMoveCurrentCell = oppHeroMove.getCurrentCell();
                Cell oppHeroMoveCurrentCell = oppHeroMoveHero.getCurrentCell();
                if (oppHeroMoveCurrentCell.isInVision() && oppHeroMoveCurrentCell.isInObjectiveZone()) {
                    int distance = virtualWorld.manhattanDistance(myHeroMoveNextCell, oppHeroMoveNextCell);
                    int distanceForSum = virtualWorld.manhattanDistance(myHeroMoveNextCell, oppHeroMoveNextCell);
                    distanceSum += distanceForSum;
                    if (distance <= myHeroCanHitMaxDistance) {
                        myHeroCanHitAnyone = true;

                        int distance2 = virtualWorld.manhattanDistance(myHeroMoveCurrentCell, oppHeroMoveCurrentCell);
                        if (distance2 > myHeroCanHitMaxDistance ){
                            canNextCellAttack = true;
//                            score += 50000;
//                            break; //if break doesn't exist my hero go to cells that have more enemy;
                        }
                    }

//                    if (oppHeroMoveWithMinimumHealth == null) {
//                        oppHeroMoveWithMinimumHealth = oppHeroMove;
//                    } else {
//                        if (oppHeroMoveHero.getCurrentHP() < oppHeroMoveWithMinimumHealth.getHero().getCurrentHP()) {
//                            oppHeroMoveWithMinimumHealth = oppHeroMove;
//                        }
//                    }
                }
            }

            if (!myHeroCanHitAnyone) {
                if (virtualWorld.getCurrentTurn()>=24) {
                    int i = 0;
                }

//                score += ((virtualWorld.getMap().getColumnNum() + virtualWorld.getMap().getRowNum() ) - distanceSum ) * 2000;
//                if (oppHeroMoveWithMinimumHealth != null) {
//                    Direction[] directions = virtualWorld.getPathMoveDirections(myHeroMoveNextCell, oppHeroMoveWithMinimumHealth.getNextCell());
//                    Direction direction = directions[0];

//                score += directions.length * (-2100);
//                    MyDirection directionThatShouldGo = Utility.castDirectionToMyDirection(direction);

//                    if (myHeroDirection.equals(directionThatShouldGo)) {
//                        Integer distance = virtualWorld.manhattanDistance(myHeroMoveNextCell, oppHeroMoveWithMinimumHealth.getNextCell());
////                        score += -Score.DISTANCE_TO_OPP_HEROES * distance;
//                        score =  2 * (-Score.MOVE_COST);
//                    }
//                }
//

//                if (move.getBeforeCell() != null) {
//                    if (move.getBeforeCell().equals(Utility.getCellFromDirection(move.getCurrentCell(), myHeroDirection, virtualWorld.getMap()))) {
//                        score += -1000;
//                    }
//                }
//                if (myHeroDirection.equals(MyDirection.FIX)) {
//                    score += 3 * Score.MOVE_COST;
//                }
                score += distanceSum * 20000 * Score.MOVE_COST;
                if (canNextCellAttack){
                    score += 50000;
                }

            }

        }
        return score;
    }

    public static Integer reduceDistanceToOppHeroesWithMinimumHealth(Hero myHero, MyDirection myHeroDirection, ArrayList<Hero> otherOurHeroes, Hero oppHero, ArrayList<Hero> oppHeroes, World virtualWorld, ArrayList<Move> copyOfMyHeroesMove, ArrayList<Move> copyOfOppHeroesMove, ArrayList<Cell> blocks) {

        Integer distanceSum;
        Integer distanceScore = 0;

        Move oppHeroMoveWithMinimumHealth = null;

        Move move = Move.findByHero(copyOfMyHeroesMove, myHero);
        Cell myHeroMoveNextCell = move.getNextCell();
        Cell myHeroMoveCurrentCell = myHero.getCurrentCell();

        for (Move oppHeroMove : copyOfOppHeroesMove) {
            Hero oppHeroMoveHero = oppHeroMove.getHero();
            Cell oppHeroMoveNextCell = oppHeroMove.getNextCell();
            Cell oppHeroMoveCurrentCell = oppHeroMove.getCurrentCell();

            if (oppHeroMoveNextCell.isInVision()) {
                if (oppHeroMoveWithMinimumHealth == null) {
                    oppHeroMoveWithMinimumHealth = oppHeroMove;
                } else {
                    if (oppHeroMoveHero.getCurrentHP() < oppHeroMoveWithMinimumHealth.getHero().getCurrentHP()) {
                        oppHeroMoveWithMinimumHealth = oppHeroMove;
                    }
                }
            }
        }

        if (oppHeroMoveWithMinimumHealth != null) {

            Cell oppHeroMoveWithMinimumHealthNextCell = oppHeroMoveWithMinimumHealth.getNextCell();
            Cell oppHeroMoveWithMinimumHealthCurrentCell = oppHeroMoveWithMinimumHealth.getCurrentCell();

            distanceSum = virtualWorld.manhattanDistance(myHeroMoveNextCell, oppHeroMoveWithMinimumHealthNextCell);

            int currentDistance = virtualWorld.manhattanDistance(myHeroMoveCurrentCell, oppHeroMoveWithMinimumHealthCurrentCell);
            int myHeroCanHitMaxDistance = Utility.CanHitMaxDistance(myHero);
            boolean canHitOppHeroWithMinimumHealth = myHeroCanHitMaxDistance < currentDistance && myHeroCanHitMaxDistance != -1;

            if (!canHitOppHeroWithMinimumHealth) {
                distanceScore += distanceSum * Score.DISTANCE_COST;
                if (myHeroDirection.equals(MyDirection.FIX)) {
                    distanceScore += 2 * Score.MOVE_COST;
                }
            }
        }

        return distanceScore;
    }
}
