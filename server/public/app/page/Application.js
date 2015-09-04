var Application = React.createClass({
    mixins: [Reflux.ListenerMixin],
    componentDidMount: function() {
        this.listenTo(Actions.registerSubmit.success, this.onLogin);
        this.listenTo(Actions.loginSubmit.success, this.onLogin);
        this.listenTo(Actions.joinPlay.success, this.onJoinPlay);
        this.listenTo(Actions.leavePlay, this.onLogin);
    },
    getInitialState: function() {
        return {
            currentPage: "landing",
            loggedIn: false,
            gameId: null
        }
    },
    onLogin: function(){
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