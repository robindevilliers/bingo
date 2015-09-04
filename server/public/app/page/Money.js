

var Money = React.createClass({
    render: function() {
        var amount = amountString(this.props.amount);
        return <span>{amount}</span>;

    }
});