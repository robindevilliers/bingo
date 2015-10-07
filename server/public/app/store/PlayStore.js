

var PlayStore = Reflux.createStore({
    init: function() {
        this.listenTo(Actions.joinPlay, 'joinPlay');
        this.listenTo(Actions.playLoad, 'playLoad');
        this.listenTo(Actions.anteIn, 'anteIn');
    },
    joinPlay: function(data) {

        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "play",
            dataType: "json",
            data: data,
            success: function(response, status, xhr){
                Actions.joinPlay.success(data);
            },
            error: function(xhr, status, error){
                Actions.joinPlay.failed();
            }
        });
    },
    playLoad: function(data){
        $.ajax({
                method: "GET",
                contentType: "application/json",
                url: "play",
                data: {gameId: data},
                success: function(data, status, xhr){
                    Actions.playLoad.success(data);
                },
                error: GeneralErrorHandler(function(xhr, status, error){
                    Actions.playLoad.failed();
                })
            });
    },
    anteIn: function(data){
        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "play/ante-in",
            dataType: "json",
            data: JSON.stringify(data),
            success: function(data, status, xhr){
                Actions.anteIn.success(data);
            },
            error: GeneralErrorHandler(function(xhr, status, error){

                if (xhr.responseJSON.errorCode == 'CLIENT_GAME_NOT_FOUND'){
                    Actions.anteIn.failed(xhr.responseJSON);
                } else if (xhr.responseJSON.errorCode == 'CLIENT_INVALID_TICKET_INDEXES'){
                    Actions.anteIn.failed(xhr.responseJSON);
                } else if (xhr.responseJSON.errorCode == 'CLIENT_GAME_NOT_FOUND'){
                    Actions.anteIn.failed(xhr.responseJSON);
                } else if (xhr.responseJSON.errorCode == 'CLIENT_INSUFFICIENT_FUNDS'){
                    Actions.anteIn.failed(xhr.responseJSON);
                }
            })
        });
    }
});