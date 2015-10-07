

var Actions = Reflux.createActions({
    "registerSubmit":  {children: ["success","failed"]},
    "loginSubmit":  {children: ["success","failed"]},
    "lobbyLoad":  {children: ["success","failed"]},
    "joinPlay": {children: ["success", "failed"]},
    "leavePlay": {},
    "anteIn": {children: ["success", "failed"]},
    "drawNumber": {},
    "gameStart": {},
    "gameEnd": {},
    "tick": {},
    "topup": {},
    "topupSubmit": {children: ["success","failed"]},
    "playLoad": {children: ["success", "failed"]},
    "displayLobby": {},
    "sendMessage": {children: ["success", "failed"]},
    "pollMessages": {children: ["success", "failed"]},
    "generalError": {},
});

function amountString(amount){
    var pounds = Math.floor( amount / 100);

    if (pounds >= 1){
        return '£' + pounds + '.' +  (amount - (pounds * 100));
    } else {
        return '' + amount + 'p'
    }
}


function GeneralErrorHandler(func) {

    return function(xhr, status, error){

        if (xhr.readyState != 4){
            //we have connection difficulties
            //TODO - might be a good idea to raise a connection issue page
            return;
        }

        if (xhr.responseJSON.errorCode == "SERVER_UNKNOWN_ERROR"){
           Actions.generalError(xhr.responseJSON);
        } else if (xhr.responseJSON.errorCode == "CLIENT_NOT_AUTHORISED"){
           window.location.pathname = "/";
        } else {
            func(xhr, status, error);
        }
    }
}


