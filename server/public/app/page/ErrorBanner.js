
var ErrorBanner = React.createClass({
    render: function() {

        if (this.props.errorMessage){
            return (
                <div className="panel panel-danger">
                    <div className="panel-heading">Error</div>
                    <div className="panel-body">
                        {this.props.errorMessage}
                    </div>
                </div>
            );
        } else {
            return <div/>;
        }
    }
});