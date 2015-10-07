
var AuthenticationStore = Reflux.createStore({
    init: function() {
        this.listenTo(Actions.loginSubmit, 'loginSubmit');
    },
    loginSubmit: function(data) {
        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "login",
            dataType: "json",
            data: JSON.stringify(data),
            success: function(data, status, xhr){
                Actions.loginSubmit.success();
            },
            error: GeneralErrorHandler(function(xhr, status, error){
                if (xhr.responseJSON.errorCode == 'CLIENT_INVALID_INPUT'){
                    Actions.loginSubmit.failed(xhr.responseJSON);
                } else if (xhr.responseJSON.errorCode == 'CLIENT_INVALID_CREDENTIALS'){
                    Actions.loginSubmit.failed(xhr.responseJSON);
                } else if (xhr.responseJSON.errorCode == 'CLIENT_AUTHENTICATION_FAILURE_LIMIT_EXCEEDED'){
                    Actions.loginSubmit.failed(xhr.responseJSON);
                }
            })
        });
    }
});