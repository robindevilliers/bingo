

var TimeLeft = React.createClass({
    getInitialState : function(){
        return {
            reference: null,
            currentTime: new Date().getTime()
        };
    },
    componentDidMount: function() {
        this.setState({reference: setInterval(this.updateTime, 200)});
    },
    componentWillUnmount: function() {
        clearInterval(this.state.reference);
    },
    updateTime : function(){
        this.setState({currentTime: new Date().getTime()});
    },
    render: function() {

        var timeInSeconds = Math.floor(( this.props.time - this.state.currentTime) / 1000);

        if (timeInSeconds < 0){
            return <span>In progress</span>;
        }

        var quotient = timeInSeconds / 60;
        var seconds = quotient % 1;
        var minutes = quotient - seconds;
        seconds = Math.floor(seconds * 60);

        var minutesPart = minutes > 0 ? '' + minutes + ' m' : '';
        var secondsPart = seconds > 0 ? '' + seconds + ' s' : '';
        return (<span>{minutesPart} {secondsPart}</span>);
    }
});