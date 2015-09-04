
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
                Actions.loginSubmit.success()
            },
            error: function(xhr, status, error){
                console.log('failed login ' + status);
                Actions.loginSubmit.failed()
            }
        });
    }
});