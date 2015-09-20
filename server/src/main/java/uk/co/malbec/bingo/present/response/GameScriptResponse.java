package uk.co.malbec.bingo.present.response;

import uk.co.malbec.bingo.present.response.DrawResponse;

import java.util.List;

public class GameScriptResponse {

    private List<DrawResponse> draws;

    public GameScriptResponse() {
    }

    public GameScriptResponse(List<DrawResponse> draws){
        this.draws = draws;
    }

    public List<DrawResponse> getDraws() {
        return draws;
    }


}
