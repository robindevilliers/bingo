
var ChatStore = Reflux.createStore({
    init: function() {
        this.listenTo(Actions.sendMessage, 'sendMessage');
        this.listenTo(Actions.pollMessages, 'pollMessages');
    },
    sendMessage: function(data) {

        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "send-message",
            dataType: "json",
             data: JSON.stringify(data),
            success: function(data, status, xhr){
                Actions.sendMessage.success(data)
            },
            error: function(xhr, status, error){
                //TODO - do failures at some point
            }
        });
    },
    pollMessages: function(data){
        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "poll-messages",
            dataType: "json",
            data: JSON.stringify(data),
            success: function(data, status, xhr){
                Actions.pollMessages.success(data)
            },
            error: function(xhr, status, error){
                //TODO - do failures at some point
            }
        });
    }
});