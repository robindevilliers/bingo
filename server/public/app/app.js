

var Actions = Reflux.createActions({
    "registerSubmit":  {children: ["success","failed"]},
    "loginSubmit":  {children: ["success","failed"]},
    "lobbyLoad":  {children: ["success","failed"]},
    "joinPlay": {children: ["success", "failed"]},
    "playLoad": {children: ["success", "failed"]},
    "leavePlay": {},
    "anteIn": {children: ["success", "failed"]},
    "drawNumber": {},
    "drawNumber": {},
    "gameStart": {},
    "gameEnd": {},
    "tick": {},

});








function amountString(amount){
    var pounds = Math.floor( amount / 100);

    if (pounds >= 1){
        return '£' + pounds + ':' +  (amount - (pounds * 100));
    } else {
        return '' + amount + 'p'
    }
}