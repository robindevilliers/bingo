

var LobbyStore = Reflux.createStore({
    init: function() {
        this.listenTo(Actions.lobbyLoad, 'lobbyLoad');
    },
    lobbyLoad: function(data) {

        $.ajax({
            method: "GET",
            contentType: "application/json",
            url: "lobby",
            dataType: "json",
            success: function(data, status, xhr){
                Actions.lobbyLoad.success(data);
            },
            error: GeneralErrorHandler(function(xhr, status, error){

                //TODO
                Actions.lobbyLoad.failed();

            })
        });
    }
});