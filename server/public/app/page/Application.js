var Application = React.createClass({
    mixins: [Reflux.ListenerMixin],
    componentDidMount: function() {
        this.listenTo(Actions.registerSubmit.success, this.onDisplayLobby);
        this.listenTo(Actions.loginSubmit.success, this.onDisplayLobby);
        this.listenTo(Actions.joinPlay.success, this.onJoinPlay);
        this.listenTo(Actions.leavePlay, this.onDisplayLobby);
        this.listenTo(Actions.topup, this.onTopup);
        this.listenTo(Actions.displayLobby, this.onDisplayLobby);
        this.listenTo(Actions.generalError, this.onError);
    },
    getInitialState: function() {
        return {
            currentPage: "landing",
            loggedIn: false,
            gameId: null
        }
    },
    onDisplayLobby: function(){
        this.setState({
            currentPage: "lobby",
            loggedIn: true
        });
    },
    onJoinPlay: function(data){
        this.setState({
            currentPage: "play",
            gameId: data
        });
    },
    onTopup: function(){
        this.setState({
            currentPage: 'topup'
        });
    },
    onError: function(){
        this.setState({
            currentPage: 'error'
        });
    },
    render: function() {

        var pane = null;

        switch (this.state.currentPage){
            case 'landing':
                pane = <LandingPage />;
                break;
            case 'lobby':
                pane = <LobbyPage />;
                break;
            case 'play':
                pane = <PlayPage gameId={this.state.gameId} />;
                break;
            case 'topup':
                pane = <Topup/>;
                break;
            case 'error':
                pane = <Error/>;
                break;
        }

        return (
            <div className="container">
                <div className="navbar navbar-inverse navbar-fixed-top" role="navigation">
                    <div className="container">
                        <div className="navbar-header">
                            <a className="navbar-brand" href="#">Madhat Bingo</a>
                        </div>
                    </div>
                </div>
                {pane}
            </div>
        );
  }
});