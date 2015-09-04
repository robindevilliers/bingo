
var Ticket = React.createClass({
    getInitialState : function(){
        return {
            selected: [
                false,false,false,false,false,false,false,false,false,false,
                false,false,false,false,false,false,false,false,false,false,
                false,false,false,false,false,false,false,false,false,false,
                false,false,false,false,false,false,false,
            ]
        };
    },
    componentDidMount: function() {
        this.gameEndUnSubscribe = Actions.gameEnd.listen(this.onGameEnd);
        this.drawNumberUnSubscribe = Actions.drawNumber.listen(this.onDrawNumber);
    },
    componentWillUnmount: function() {
        this.drawNumberUnSubscribe();
        this.gameEndUnSubscribe();
    },
    select : function(){
        this.props.selectTicket(this.props.index);
    },
    onDrawNumber: function(draw){
        if (this.props.numbers == null){
            return;
        }

        for (var i = 0; i < 27; i++){
            if (this.props.numbers[i] == draw.number){
                this.state.selected[i] = true;
                this.setState({selected: this.state.selected});
            }
        }
    },
    onGameEnd: function(){
        this.setState({selected: [
            false,false,false,false,false,false,false,false,false,false,
            false,false,false,false,false,false,false,false,false,false,
            false,false,false,false,false,false,false,false,false,false,
            false,false,false,false,false,false,false,
        ]});
    },
    render: function() {


        var content = null;
        switch(this.props.mode){
            case 'unselected':
                var colour = 'blue';
                var text = null;

                if (this.props.ticketFee != null) {
                    var amount = amountString(this.props.ticketFee);
                    text = 'Select and pay ' + amount + ' to play';
                }
                content = <text x="75" y="55"  style={{'fontSize': 24, fill: colour}} transform="rotate(5)">{text}</text>;
            break;
            case 'selected':
                var colour = 'green';
                var text = 'selected';
                content = <text x="225" y="55"  style={{'fontSize': 24, fill: colour}} transform="rotate(5)">{text}</text>;
            break;
            case 'populated':
                if (this.props.numbers == null){
                    break;
                }
                var elements = [];
                for (var row = 1; row < 4; row++){
                    for (var column = 0; column < 9; column++){
                        if (this.state.selected[column*3 + (row-1)]){
                            var scratch = <ellipse cx={column*60 + 35} cy={row*35-5} rx="25" ry="15" style={{fill: 'yellow',  strokeWidth: 2}} />
                            elements.push(scratch);
                        }

                        var element = <text x={column*60 + 20} y={row*35} style={{'fontSize': 24, fill: 'black'}} >{this.props.numbers[column*3 + (row-1)]}</text>
                        elements.push(element);


                    }
                }

                var content = (
                    <g>
                    <line x1="10" y1="45" x2="540" y2="45" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="10" y1="80" x2="540" y2="80" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="70" y1="15" x2="70" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="130" y1="15" x2="130" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="190" y1="15" x2="190" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="250" y1="15" x2="250" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="310" y1="15" x2="310" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="370" y1="15" x2="370" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="430" y1="15" x2="430" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    <line x1="490" y1="15" x2="490" y2="105" style={{ 'strokeWidth': 2, stroke: 'rgb(0,0,0)'}} />
                    {elements}
                    </g>
                )
            break;
        }


        return (
            <svg onClick={this.select} width="550" height="160" version = "1.1">
                <rect  width="550" height="150" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                {content}
            </svg>
        );
    }
});