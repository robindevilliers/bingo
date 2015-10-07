
var ValidationErrors = React.createClass({
    render: function() {

        if (this.props.errors){
            var errors = this.props.errors;
            if (errors.errorCode == 'CLIENT_INVALID_INPUT'){

                var elements = [];

                $.each(errors.details, function(index){
                    var error = errors.details[index];
                    if (error.errorType == 'Size'){
                        var message = error.arguments[0] + " is an incorrect size.";
                    } else if (error.errorType == 'Pattern'){
                        var message = error.arguments[0] + " has an incorrect value.";
                    }
                    elements.push((<li>{message}</li>))
                })
                return (
                    <div className="panel panel-danger">
                        <div className="panel-heading">Validation Errors</div>
                        <div className="panel-body">
                            <ul>
                               {elements}
                            </ul>
                        </div>
                    </div>
                );
            } else {
                return  <div/>;
            }

        } else {
            return <div/>;
        }
    }
});