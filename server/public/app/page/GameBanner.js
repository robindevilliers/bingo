

var GameBanner = React.createClass({

    getInitialState : function(){
        return {
            inGame: false,
            timeInSeconds: null,
            draw: null
        };
    },
    componentDidMount: function() {
        this.gameStartUnSubscribe = Actions.gameStart.listen(this.onGameStart);
        this.gameEndUnSubscribe = Actions.gameEnd.listen(this.onGameEnd);
        this.drawNumberUnSubscribe = Actions.drawNumber.listen(this.onDrawNumber);
        this.tickUnSubscribe = Actions.tick.listen(this.onTick);
    },
    componentWillUnmount: function() {
        this.gameStartUnSubscribe();
        this.drawNumberUnSubscribe();
        this.gameEndUnSubscribe();
        this.tickUnSubscribe();
    },
    onGameStart: function(){
        this.setState({inGame: true});
        for (var i = 0 ; i < 90; i++){
            this.numbers[i] = false;
        }
    },
    numbers: [
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false,
        false,false,false,false,false,false,false,false,false,false
    ],
    onDrawNumber: function(draw){


        if (this.splash != null && this.splashTime < new Date().getTime()){
            this.splash = null;
            this.splashTime = null;
            this.splashWinnings = null;
            this.splashWinner = null;
            this.totalWinnings = null;
        }

        var prize = null;
        var winnings = null;
        var winner = null;
        for (var i = 0; i < draw.prizes.length; i++){

            if (draw.prizes[i].username === this.props.parameters.username){

                var newPrize = draw.prizes[i].prizeType;

                if (newPrize == 'FULL_HOUSE'){
                    winnings = this.props.parameters.fullHousePrize;
                } else if (newPrize == 'ONE_LINE'){
                    winnings = this.props.parameters.oneLinePrize;
                } else if (newPrize == 'TWO_LINES'){
                    winnings = this.props.parameters.twoLinesPrize;
                } else if (newPrize == 'FOUR_CORNERS'){
                    winnings = this.props.parameters.fourCornersPrize;
                }


                if (newPrize == 'FULL_HOUSE'){
                    prize = 'FULL_HOUSE';
                } else if (newPrize == 'TWO_LINES' && prize != 'FULL_HOUSE' && prize != 'LOSE'){
                    prize = 'TWO_LINES';
                } else if (newPrize == 'FOUR_CORNERS' && prize != 'FULL_HOUSE' && prize != 'TWO_LINES'  && prize != 'LOSE'){
                    prize = 'FOUR_CORNERS';
                } else if (newPrize == 'ONE_LINE' && prize == null){
                    prize = 'ONE_LINE';
                }
            } else {
                if (draw.prizes[i].prizeType === 'FULL_HOUSE'){
                    prize = 'LOSE';
                    winner = draw.prizes[i].username;
                }
            }
        }
        if (prize != null){
            this.splash = prize;
            this.splashTime = new Date().getTime() + 5000;
            this.splashWinnings = winnings;
            this.splashWinner = winner;
            this.totalWinnings = this.totalWinnings + winnings;
        }

        this.numbers[draw.number-1] = true;
        this.setState({draw: draw});

    },
    onGameEnd: function(){
        this.setState({inGame: false});
    },
    onTick: function(timeInSeconds){
        this.setState({timeInSeconds: timeInSeconds});

        if (this.splash != null && this.splashTime < new Date().getTime()){
            this.splash = null;
            this.splashTime = null;
            this.splashWinnings = null;
            this.splashTotalWinnings = null;
        }
    },
    splash: null,
    splashTime: null,
    splashWinnings: null,
    splashWinner: null,
    splashTotalWinnings: null,
    totalWinnings: 0,
    render: function() {

        if (this.splash == 'ONE_LINE'){
            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    <text x="55" y="130"  style={{'fontSize': '55', fill: 'blue'}}>You win a</text>
                    <text x="255" y="210"  style={{'fontSize': '55', fill: 'blue'}}>one line</text>
                    <text x="355" y="290"  style={{'fontSize': '55', fill: 'blue'}}>prize!!!</text>
                    <text x="400" y="350"  style={{'fontSize': '24', fill: 'blue'}}>{'You win ' + amountString(this.splashWinnings)}</text>
                </svg>
            );
        } else if (this.splash == 'TWO_LINES'){
            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    <text x="55" y="130"  style={{'fontSize': '55', fill: 'blue'}}>You win a</text>
                    <text x="255" y="210"  style={{'fontSize': '55', fill: 'blue'}}>two lines</text>
                    <text x="355" y="290"  style={{'fontSize': '55', fill: 'blue'}}>prize!!!</text>
                    <text x="400" y="350"  style={{'fontSize': '24', fill: 'blue'}}>{'You win ' + amountString(this.splashWinnings)}</text>
                </svg>
            );
        } else if (this.splash == 'FULL_HOUSE'){
            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    <text x="55" y="210"  style={{'fontSize': '150', fill: 'blue'}}>Bingo!!!</text>
                    <text x="300" y="350"  style={{'fontSize': '24', fill: 'blue'}}>{'You win ' + amountString(this.splashWinnings)}</text>
                    <text x="100" y="385"  style={{'fontSize': '20', fill: 'blue'}}>{'Your total winnings for this game has been ' + amountString(this.totalWinnings)}</text>
                </svg>
            );
        } else if (this.splash == 'FOUR_CORNERS'){
            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    <text x="55" y="130"  style={{'fontSize': '55', fill: 'blue'}}>You win a</text>
                    <text x="185" y="210"  style={{'fontSize': '55', fill: 'blue'}}>four corners</text>
                    <text x="355" y="290"  style={{'fontSize': '55', fill: 'blue'}}>prize!!!</text>
                    <text x="400" y="350"  style={{'fontSize': '24', fill: 'blue'}}>{'You win ' + amountString(this.splashWinnings)}</text>
                </svg>
            );
        } else if (this.splash == 'LOSE'){
            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    <text x="55" y="130"  style={{'fontSize': '36', fill: 'blue'}}>Congratulations to </text>
                    <text x="355" y="180"  style={{'fontSize': '36', fill: 'blue'}}>{this.splashWinner + '!!!!'}</text>

                    <text x="155" y="230"  style={{'fontSize': '36', fill: 'blue'}}>Better luck next time.</text>
                    <text x="100" y="385"  style={{'fontSize': '20', fill: 'blue'}}>{'Your total winnings for this game has been ' + amountString(this.totalWinnings)}</text>
                </svg>
            );
        }



        if (!this.state.inGame){
            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    <text x="55" y="130"  style={{'fontSize': '55', fill: 'blue'}}>Stand a chance</text>
                    <text x="255" y="210"  style={{'fontSize': '55', fill: 'blue'}}>to win</text>
                    <text x="155" y="290"  style={{'fontSize': '55', fill: 'blue'}}>great prizes!!!!!!</text>
                </svg>
            );
        } else {

            var elements = [];
            for (var row = 0; row < 6; row++){
                for (var column = 0; column < 15; column++){
                    var element = <text x={column*50 + 10} y={row*35+200} style={{'fontSize': 24, fill: 'black'}} >{this.numbers[row*15+column] ? row*15+column + 1 : '-'}</text>
                    elements.push(element);
                }
            }

            var headerText = null;
            if (this.totalWinnings > 0){
                headerText = <g>
                    <text x="35" y="90"  style={{'fontSize': '36', fill: 'blue'}}>{'So far you have won ' + amountString(this.totalWinnings)}</text>
                    <text x="335" y="135"  style={{'fontSize': '36', fill: 'blue'}}>{' and counting...'}</text>
                </g>;
            }  else {
                headerText = <text x="55" y="130"  style={{'fontSize': '85', fill: 'blue'}}>Good Luck!!!!!!</text>;
            }

            return (
                <svg width="750" height="400">
                    <rect width="750" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    {headerText}
                    {elements}
                </svg>
            );
        }
    }
});




