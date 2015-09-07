package uk.co.malbec.bingo;


import uk.co.malbec.bingo.model.*;
import uk.co.malbec.bingo.present.response.*;

import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public class Converters {

    public static Function<Game, GameResponse> toGameResponse(){
        return game ->  new GameResponse(
                game.getId(),
                game.getTitle(),
                game.getStagingTime(),
                game.getTicketFee(),
                game.getPlayerLimit()
        );
    }

    public static Function<Ticket, TicketResponse> toTicketResponse(){
       return ticket -> new TicketResponse(
                ticket.getId(),
                ticket.getKey(),
                ticket.getIndex(),
                ticket.getNumbers()
       );
    }

    public static Function<Prize, PrizeResponse> toPrizeResponse(){
        return prize -> new PrizeResponse(
                prize.getUsername(),
                prize.getPrizeType()
        );
    }

    public static Function<Draw, DrawResponse> toDrawResponse(){
        return draw -> new DrawResponse(
                draw.getNumber(),
                draw.getPrizes().stream().map(toPrizeResponse()).collect(toList())
        );
    }

    public static Function<GameScript, GameScriptResponse> toGameScriptView(){
        return gameScript -> new GameScriptResponse(
                gameScript.getDraws().stream().map(toDrawResponse()).collect(toList())
        );
    }

    public static Function<Play, PlayResponse> toPlayResponse(){
        return play -> new PlayResponse(
                play.getId(),
                toGameResponse().apply(play.getGame()),
                play.getTickets().stream().map(toTicketResponse()).collect(toList()),
                play.getStartTime(),
                play.getEndTime(),
                ofNullable(play.getGameScript()).map(toGameScriptView()).orElse(null)
        );
    }

}
