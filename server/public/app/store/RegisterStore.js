
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
            error: function(xhr, status, error){
                Actions.registerSubmit.failed({code: 'USERNAME_TAKEN'})
            }
        });
    }
});