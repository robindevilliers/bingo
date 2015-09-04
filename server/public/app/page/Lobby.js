var LobbyPage = React.createClass({
    mixins: [Reflux.ListenerMixin],
    componentDidMount: function() {
        this.listenTo(Actions.lobbyLoad.success, this.onLobbyLoad);
        Actions.lobbyLoad();
    },
    componentWillUnmount: function() {
        clearInterval(this.state.reference);
    },
    getInitialState: function() {
        return {
            reference: setInterval(Actions.lobbyLoad, 5000),
            plays: []
        };
    },
    onLobbyLoad: function(data){
        this.setState({plays: data});
    },
    onJoin: function(e){

        Actions.joinPlay(e.target.id);
    },
    render: function() {

        var index;
        var tableRows = [];

        for (index = 0; index < this.state.plays.length; ++index) {
            var play = this.state.plays[index];

            var joinButton = new Date().getTime() > (play.startTime - 20000) ? '' : <button type="button" id={play.game.id} onClick={this.onJoin} className="btn btn-default">Join</button>;

            tableRows.push(<tr>
                <td><h4>{play.game.title}</h4></td>
                <td><TimeLeft time={play.startTime} /></td>
                <td><Money amount={play.game.ticketFee}/></td>
                <td>{joinButton}</td>
                </tr>
                );
        }

        return (
            <div className="container" >
                <table className="table table-hover">
                <thead>
                    <tr> <th>Name</th> <th>Time until start</th> <th>Ticket fee</th> </tr>
                    </thead>
                    <tbody>
                    {tableRows}
                    </tbody>
                </table>
            </div>
        );
    }
});


