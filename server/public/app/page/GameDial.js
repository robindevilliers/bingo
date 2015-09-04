
var GameDial = React.createClass({
    getInitialState : function(){
        return {
            inGame: false,
            draw: null,
            timeInSeconds: 0
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
    },
    onDrawNumber: function(draw){
        this.setState({draw: draw});
    },
    onGameEnd: function(){
        this.setState({inGame: false, draw: null});
    },
    onTick: function(timeInSeconds){
        this.setState({timeInSeconds: timeInSeconds});
    },
    render: function() {
        if (this.props.parameters.startTime == null){
            return <svg width="500" height="500"/>;
        }

        if (this.state.inGame){
            if (this.state.draw == null){
                return (
                    <svg width="350" height="400">
                        <rect width="350" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />

                        <text x="85" y="55"  style={{'fontSize': 24, fill: 'blue'}}>The Game</text>
                        <text x="75" y="95"  style={{'fontSize': 24, fill: 'blue'}}>Is About To</text>
                        <text x="95" y="135"  style={{'fontSize': 24, fill: 'blue'}}>Begin!!!!</text>
                        <g dangerouslySetInnerHTML={{__html: '<image xlink:href="images/logo.jpg" x="65" y="180" height="200px" width="200px"/>'}} ></g>

                    </svg>
                );
            } else {
                var x = 5;
                if (this.state.draw.number < 10){
                    x = x + 80;
                } else if (this.state.draw.number < 20){
                    x = x + 65;
                } else if (this.state.draw.number < 30){
                    x = x + 25;
                } else if (this.state.draw.number < 40){
                    x = x + 25;
                }

                if (this.state.draw.number % 10 == 1){
                    x = x + 65;
                } else if (this.state.draw.number % 10 == 2){
                    x = x + 25;
                } else if (this.state.draw.number % 10 == 3){
                    x = x + 25;
                }

                var numberInWords = this.phrases[this.state.draw.number][0];
                var catchPhrase = this.phrases[this.state.draw.number][1];

                var xPrizeLine = 0;
                var prizeLineOne = '';
                var prizeLineTwo = '';
                for (var i = 0; i < this.state.draw.prizes.length; i++){
                    var prize = this.state.draw.prizes[i];
                    if (prize.username === this.props.parameters.username){
                        if (prize.prizeType === 'ONE_LINE'){
                            prizeLineOne = 'You win a';
                            prizeLineTwo = 'One Line Prize!';
                            xPrizeLine = 85;
                        } else if (prize.prizeType === 'TWO_LINES') {
                            prizeLineOne = 'You win a';
                            prizeLineTwo = 'Two Lines Prize!';
                            xPrizeLine = 85;
                        } else if (prize.prizeType === 'FOUR_CORNERS') {
                            prizeLineOne = 'You win a';
                            prizeLineTwo = 'Four Corners Prize';
                            xPrizeLine = 55;
                        } else if (prize.prizeType === 'FULL_HOUSE') {
                            prizeLineOne = 'You win the';
                            prizeLineTwo = 'House!!!!!!';
                            xPrizeLine = 85;
                        }
                    }
                }
                return (
                    <svg width="350" height="400">
                        <rect width="350" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                        <text x={x} y="190"  style={{'fontSize': '200', fill: 'blue'}}>{this.state.draw.number}</text>
                        <text x="25" y="230"  style={{'fontSize': '24', fill: 'blue'}}>{numberInWords + ','}</text>
                        <text x={150 - (catchPhrase.length*5)} y="270"  style={{'fontSize': '24', fill: 'blue'}}>{catchPhrase}</text>
                        <text x={300 - (numberInWords.length*15)} y="310"  style={{'fontSize': '24', fill: 'blue'}}>{numberInWords + '!!!!'}</text>

                    </svg>
                );
            }

        }

        var quotient = this.state.timeInSeconds / 60;
        var seconds = quotient % 1;
        var minutes = quotient - seconds;
        seconds = Math.floor(seconds * 60);
        var minutesPart = minutes > 0 ? '' + minutes + ' m' : '';
        var secondsPart = seconds > 0 ? '' + seconds + ' s' : '';
        var countDownText = 'Starting in ' + minutesPart + ' ' + secondsPart;
        var totalPotText = 'Total Pot: ' + amountString(this.props.parameters.totalPot);
        var yourWalletText = 'Wallet: ' + amountString(this.props.parameters.yourWallet);
        var yourBetText = 'Current Bet: ' + amountString(this.props.parameters.yourBet);
        var fullHousePrizeText = 'Full House: ' + amountString(this.props.parameters.fullHousePrize);
        var twoLinesPrizeText = '2 Lines: ' + amountString(this.props.parameters.twoLinesPrize);
        var oneLinePrizeText = '1 Line: ' + amountString(this.props.parameters.oneLinePrize);
        var fourCornersPrizeText = '4 Corners: ' + amountString(this.props.parameters.fourCornersPrize);

        return (
            <svg width="350" height="400">
                <rect width="350" height="400" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                <text x="35" y="55"  style={{'fontSize': 24, fill: 'blue'}}>{countDownText}</text>;
                <text x="35" y="95"  style={{'fontSize': 24, fill: 'blue'}}>{totalPotText}</text>;
                <text x="35" y="135"  style={{'fontSize': 24, fill: 'blue'}}>{yourWalletText}</text>;
                <text x="35" y="175"  style={{'fontSize': 24, fill: 'blue'}}>{yourBetText}</text>;
                <text x="35" y="215"  style={{'fontSize': 24, fill: 'blue'}}>Prizes:</text>;
                <text x="35" y="255"  style={{'fontSize': 24, fill: 'blue'}}>{fullHousePrizeText}</text>;
                <text x="35" y="295"  style={{'fontSize': 24, fill: 'blue'}}>{twoLinesPrizeText}</text>;
                <text x="35" y="335"  style={{'fontSize': 24, fill: 'blue'}}>{oneLinePrizeText}</text>;
                <text x="35" y="375"  style={{'fontSize': 24, fill: 'blue'}}>{fourCornersPrizeText}</text>;
            </svg>
        );
    },
    phrases: {
        1 : ['One','The pun is'],
        2 : ['Two','Me and you'],
        3 : ['Three','You and me'],
        4 : ['Four','Knock at the door'],
        5 : ['Five','Man alive'],
        6 : ['Six','Tom Mix'],
        7 : ['Seven','Lucky'],
        8 : ['Eight','Garden gate'],
        9 : ['Nine','Doctor\'s Orders'],
        10 : ['Ten','David\'s Den'],
        11 : ['Eleven','Legs'],
        12 : ['Twelve','One dozen'],
        13 : ['Thirteen','Unlucky for some'],
        14 : ['Fourteen','Valentine\s day'],
        15 : ['Fifteen','Rugby team'],
        16 : ['Sixteen','Sweet sixteen'],
        17 : ['Seventeen','Dancing queen'],
        18 : ['Eighteen','Coming of age'],
        19 : ['Nineteen','Goodbye teens'],
        20 : ['Twenty','Getting plenty'],
        21 : ['Twenty-One','If only I was...'],
        22 : ['Twenty-Two','Two little ducks'],
        23 : ['Twenty-Three','A duck and a flea'],
        24 : ['Twenty-Four','Two dozen'],
        25 : ['Twenty-Five','Duck and dive'],
        26 : ['Twenty-Six','Pick and mix'],
        27 : ['Twenty-Seven','Gateway to heaven'],
        28 : ['Twenty-Eight','In a state'],
        29 : ['Twenty-Nine','Rise and shine'],
        30 : ['Thirty','Flirty'],
        31 : ['Thirty-One','Get up and run'],
        32 : ['Thirty-Two','Buckle my shoe'],
        33 : ['Thirty-Three','Two little fleas'],
        34 : ['Thirty-Four','Dirty whore'],
        35 : ['Thirty-Five','Jump and jive'],
        36 : ['Thirty-Six','Three dozen'],
        37 : ['Thirty-Seven','A flea in heaven'],
        38 : ['Thirty-Eight','Christmas cake'],
        39 : ['Thirty-Nine','All the steps'],
        40 : ['Forty','Naughty'],
        41 : ['Forty-One','Life\'s begun'],
        42 : ['Forty-Two','Winne the Pooh'],
        43 : ['Forty-Three','Down on your knees'],
        44 : ['Forty-Four','All the fours'],
        45 : ['Forty-Five','Halfway there'],
        46 : ['Forty-Six','Up to tricks'],
        47 : ['Forty-Seven','Four and seven'],
        48 : ['Forty-Eight','Four dozen'],
        49 : ['Forty-Nine','Rise and shine'],
        50 : ['Fifty','Bull\s eye'],
        51 : ['Fifty-One','I love my Mum'],
        52 : ['Fifty-Two','Chicken Vindaloo'],
        53 : ['Fifty-Three','The Joker'],
        54 : ['Fifty-Four','Clean the floor'],
        55 : ['Fifty-Five','All the fives'],
        56 : ['Fifty-Six','Was she worth it?'],
        57 : ['Fifty-Seven','All the beans'],
        58 : ['Fifty-Eight','Make them wait'],
        59 : ['Fifty-Nine','Brighton line'],
        60 : ['Sixty','Five dozen'],
        61 : ['Sixty-One','Baker\'s bun'],
        62 : ['Sixty-Two','Turn on the screw'],
        63 : ['Sixty-Three','Tickle me'],
        64 : ['Sixty-Four','Red raw'],
        65 : ['Sixty-Five','Stop work'],
        66 : ['Sixty-Six','All the sixes'],
        67 : ['Sixty-Seven','Made in heaven'],
        68 : ['Sixty-Eight','Saving grace'],
        69 : ['Sixty-Nine','Any way up'],
        70 : ['Seventy','Blind'],
        71 : ['Seventy-One','Band on the drum'],
        72 : ['Seventy-Two','A crutch and duck'],
        73 : ['Seventy-Three','Under the tree'],
        74 : ['Seventy-Four','Candy store'],
        75 : ['Seventy-Five','Big daddy'],
        76 : ['Seventy-Six','Trombones'],
        77 : ['Seventy-Seven','All the sevens'],
        78 : ['Seventy-Eight','Heaven\'s gate'],
        79 : ['Seventy-Nine','One more time'],
        80 : ['Eighty','Four score'],
        81 : ['Eighty-One','Fat lady and little wee'],
        82 : ['Eighty-Two','Straight on through'],
        83 : ['Eighty-Three','Fat lady and flea'],
        84 : ['Eighty-Four','Seven dozen'],
        85 : ['Eighty-Five','Staying alive'],
        86 : ['Eighty-Six','Between the sticks'],
        87 : ['Eighty-Seven','Fat lady and crutch'],
        88 : ['Eighty-Eight','Two fat ladies'],
        89 : ['Eighty-Nine','Nearly there'],
        90 : ['Ninety','Top of the house']
    }
});