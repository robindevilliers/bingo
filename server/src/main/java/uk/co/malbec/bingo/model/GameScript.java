package uk.co.malbec.bingo.model;


import uk.co.malbec.bingo.model.Draw;

import java.util.ArrayList;
import java.util.List;

public class GameScript {

    private List<Draw> draws = new ArrayList<>();

    public List<Draw> getDraws() {
        return draws;
    }

    public void add(Draw draw){
        draws.add(draw);
    }
}
