var PlayPage = React.createClass({
    mixins: [Reflux.ListenerMixin],
    componentDidMount: function() {
        this.listenTo(Actions.playLoad.success, this.onPlayLoad);
        this.listenTo(Actions.anteIn.success, this.onPlayLoad);
        Actions.playLoad(this.props.gameId);
    },
    componentWillUnmount: function() {
        clearInterval(this.state.reference);
    },
    getInitialState: function() {
        return {
            tickets: {
                1: { selected: false },
                2: { selected: false },
                3: { selected: false },
                4: { selected: false },
                5: { selected: false },
                6: { selected: false }
            },
            play: null,
            reference: setInterval(this.onInterval, 200),
            displayChat: false
        };
    },
    onPurchase: function(event){
        Actions.anteIn({
            gameId: this.props.gameId,
            tickets: this.state.tickets
        });
    },
    onLeave: function(event){
        Actions.leavePlay();
    },
    onPlayLoad: function(data){
        this.setState({play: data});
    },
    onToggleChat: function(event){
        this.setState({displayChat: !this.state.displayChat});
    },
    selectTicket: function(index){
        var hasTickets = this.state.play != null && this.state.play.tickets != null && !$.isEmptyObject(this.state.play.tickets);
        if (hasTickets){
            return;
        }

        var newTickets =  jQuery.extend(true, {}, this.state.tickets);
        newTickets[index].selected = !newTickets[index].selected;
        this.setState({tickets: newTickets});

    },
    drawIndex: 0,
    intervalIndex: 0,
    inGame: false,
    timeInSeconds: 0,
    onInterval: function(){
        if (this.state.play == null){
            return;
        }

        if (this.intervalIndex == 15){
            this.intervalIndex = 0;
            if (this.state.play.gameScript != null){

                var draw = this.state.play.gameScript.draws[this.drawIndex];

                if (draw == null){
                    Actions.playLoad(this.props.gameId);
                    Actions.gameEnd();
                    this.inGame = false;
                    this.setState({play: null});
                    this.drawIndex = 0;
                } else {
                    Actions.drawNumber(draw);
                }

                this.drawIndex = this.drawIndex + 1;

            } else {
                Actions.playLoad(this.props.gameId);
                return;
            }

        }

        this.intervalIndex++;



        var timeInSeconds = Math.floor(( this.state.play.startTime - new Date().getTime()) / 1000);
        var inGame = timeInSeconds <= 0;

        if (!this.inGame){
            if (inGame){
                Actions.gameStart();
            }
        } else {
            if (!inGame){
                Actions.gameEnd();
            }
        }
        this.inGame = inGame;

        if (!this.inGame){
            if (timeInSeconds != this.timeInSeconds){
                this.timeInSeconds = timeInSeconds;
                Actions.tick(timeInSeconds);
            }
        }

    },
    render: function() {

        function ticketMode(index, state){
            var hasTickets = state.play != null && state.play.tickets != null && !$.isEmptyObject(state.play.tickets);

            if (hasTickets){
                if (state.play.tickets[index] != null){
                    return 'populated';
                } else {
                    return 'unpopulated';
                }
            } else {
                return state.tickets[index].selected ? 'selected' : 'unselected'
            }
        }

        function getNumbers(index, state){
            if (state.play == null){
                return null;
            }

            if (state.play.tickets == null){
                return null;
            }

            if (state.play.tickets[index] == null){
                return null;
            }

            return state.play.tickets[index].numbers;
        }


        var hasTickets = false;
        var params = {
            ticketFee: null,
            startTime: null,
            totalPot: null,
            yourWallet: null,
            yourBet: null,
            fullHousePrize: null,
            twoLinesPrize: null,
            oneLinePrize: null,
            fourCornersPrize: null,
            username: null
        }

        if (this.state.play != null){
            hasTickets = !$.isEmptyObject(this.state.play.tickets);

            params.ticketFee = this.state.play.game.ticketFee;
            params.startTime = this.state.play.startTime;
            params.totalPot = this.state.play.totalPot;
            params.yourWallet = this.state.play.yourWallet;
            params.yourBet = this.state.play.yourBet;
            params.fullHousePrize = this.state.play.fullHousePrize;
            params.twoLinesPrize = this.state.play.twoLinesPrize;
            params.oneLinePrize = this.state.play.oneLinePrize;
            params.fourCornersPrize = this.state.play.fourCornersPrize;
            params.username = this.state.play.username;
        }


        var enableAnteIn = this.state.tickets[1].selected ||
            this.state.tickets[2].selected ||
            this.state.tickets[3].selected ||
            this.state.tickets[4].selected ||
            this.state.tickets[5].selected ||
            this.state.tickets[6].selected;




        var anteInButton = '';
        if (!hasTickets){
            anteInButton = <button type="button" onClick={this.onPurchase} className="btn btn-lg btn-primary" disabled={!enableAnteIn}>Ante In!</button>;
        }


        var content = null;
        if (this.state.displayChat) {
            var chatRoom = null;
            var username = null;
            if (this.state.play != null){
                chatRoom = this.state.play.game.title;
                username = this.state.play.username;
            }
            content = (
                <div className="row">
                    <div className="col-md-12">
                        <ChatPane chatRoom={chatRoom} username={username}/>
                    </div>
                </div>
            );
        } else {
            content = (

                <div className="row">
                    <div className="col-md-6">
                        <div className="row">
                            <Ticket index="1" ticketFee={params.ticketFee} mode={ticketMode(1, this.state)} numbers={getNumbers(1, this.state)} selectTicket={this.selectTicket}/>
                        </div>
                        <div className="row">
                            <Ticket index="2" ticketFee={params.ticketFee} mode={ticketMode(2, this.state)} numbers={getNumbers(2, this.state)} selectTicket={this.selectTicket}/>
                        </div>
                        <div className="row">
                            <Ticket index="3" ticketFee={params.ticketFee} mode={ticketMode(3, this.state)} numbers={getNumbers(3, this.state)} selectTicket={this.selectTicket}/>
                        </div>
                    </div>
                    <div className="col-md-6">
                        <div className="row">
                            <Ticket index="4" ticketFee={params.ticketFee} mode={ticketMode(4, this.state)} numbers={getNumbers(4, this.state)} selectTicket={this.selectTicket}/>
                        </div>
                        <div className="row">
                            <Ticket index="5" ticketFee={params.ticketFee} mode={ticketMode(5, this.state)} numbers={getNumbers(5, this.state)} selectTicket={this.selectTicket}/>
                        </div>
                        <div className="row">
                            <Ticket index="6" ticketFee={params.ticketFee} mode={ticketMode(6, this.state)} numbers={getNumbers(6, this.state)} selectTicket={this.selectTicket}/>
                        </div>
                    </div>
                </div>
            );
        }



        return (
            <div className="container">

                <div className="row">
                    <div className="col-md-4">
                        <GameDial parameters={params} />
                    </div>
                    <div className="col-md-8">
                        <GameBanner parameters={params}/>
                    </div>
                </div>
                {content}
                <div className="row">
                    &nbsp;
                </div>
                <div className="row">

                    <div className="col-md-1 pull-right">
                        <button type="button" onClick={this.onLeave} className="btn btn-lg btn-info">Leave</button>
                    </div>
                    <div className="col-md-1 pull-right">
                        {anteInButton}
                    </div>
                    <div className="col-md-2 pull-right">
                        <button type="button" onClick={this.onToggleChat} className="btn btn-lg btn-info">Toggle Chat</button>
                    </div>
                </div>

            </div>


        );
    }
});


