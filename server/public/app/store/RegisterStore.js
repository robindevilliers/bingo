
var RegisterStore = Reflux.createStore({
    init: function() {
        this.listenTo(Actions.registerSubmit, 'registerSubmit');
    },
    registerSubmit: function(data) {
        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "register",
            dataType: "json",
            data: JSON.stringify(data),
            success: function(data, status, xhr){
                Actions.registerSubmit.success()
            },
            error: GeneralErrorHandler(function(xhr, status, error, test){
                if (xhr.responseJSON.errorCode == 'CLIENT_INVALID_INPUT'){
                    Actions.registerSubmit.failed(xhr.responseJSON);
                } else {
                    //TODO
                }
            })
        });
    }
});