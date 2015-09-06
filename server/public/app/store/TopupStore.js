
var TopupStore = Reflux.createStore({
    init: function() {
        this.listenTo(Actions.topupSubmit, 'topupSubmit');
    },
    topupSubmit: function(data) {


        $.ajax({
            method: "POST",
            contentType: "application/json",
            url: "topup",
            dataType: "json",
            data: JSON.stringify(data),
            success: function(data, status, xhr){
                Actions.topupSubmit.success()
            },
            error: function(xhr, status, error){
                Actions.topupSubmit.failed({code: 'FAILED_TOPUP'})
            }
        });
    }
});