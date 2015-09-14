
var ChatPane = React.createClass({
    mixins: [Reflux.ListenerMixin],
    getInitialState : function(){
        return {
            messages: [],
            messageIndex: 0,
            reference: setInterval(this.onInterval, 500),
        };
    },
    componentDidMount: function() {
        this.listenTo(Actions.pollMessages.success, this.onPollMessagesSuccess);
    },
    componentWillUnmount: function() {
        clearInterval(this.state.reference);
    },
    onInterval: function(){
        if (this.props.chatRoom != null){
            Actions.pollMessages({chatRoom: this.props.chatRoom, messageIndex: this.state.messageIndex});
        }
    },
    onPollMessagesSuccess: function(data){
        var messages = this.state.messages;
        var messageIndex = this.state.messageIndex;
        jQuery.each(data.messages, function(index){
            messages.unshift(data.messages[index]);
            messageIndex = data.messages[index].messageIndex;
        });
        this.setState({messages: messages, messageIndex: messageIndex});
    },
    onSend : function(event){
        var message = $('#input').val();
        $('#input').val('');
        $('#input').focus();
        Actions.sendMessage({chatRoom: this.props.chatRoom, username: this.props.username, message: message});
    },
    render: function() {
            var index;
            var chatRows = [];
            var offset = 440;

            for (index = 0; index < this.state.messages.length; ++index) {
                chatRows.push(<text x="15" y={offset} fill="black">{this.state.messages[index].username + ' : ' + this.state.messages[index].message}</text>);
                offset = offset - 20;
            }

        return (
        <div>
            <div className="row text-center">
                <svg onClick={this.select} width="1150" height="450" version="1.1">
                    <rect  width="1150" height="450" style={{fill: 'rgb(255,255,255)', 'strokeWidth': 3, stroke: 'rgb(0,0,0)'}} />
                    {chatRows}
                </svg>
            </div>
            <div className="row">
                <div className="col-md-11">
                    <input className="form-control input-normal" type="text" name="input" id="input" />
                </div>
                <div className="col-md-1">
                    <button type="button" onClick={this.onSend} className="btn btn-md btn-primary" >Send</button>
                </div>
            </div>
        </div>
        );
    }
});