
var Login = React.createClass({
    mixins: [Reflux.ListenerMixin],
    getInitialState : function(){
        return {
            username: '',
            password: '',
            errorMessage: null,
            errors: null
        };
    },
    componentDidMount: function() {
        this.listenTo(Actions.loginSubmit.failed, this.onLoginFail);
    },
    onLoginFail: function(error) {
        if (error.errorCode == "CLIENT_INVALID_CREDENTIALS"){
            this.setState({errorMessage: 'Invalid username or password supplied.'});
            this.setState({errors: null});
        } else if (error.errorCode == "CLIENT_AUTHENTICATION_FAILURE_LIMIT_EXCEEDED"){
           this.setState({errorMessage: 'Account locked.  Try again in 10 minutes'});
           this.setState({errors: null});
        } else if (error.errorCode == "CLIENT_INVALID_INPUT"){
            this.setState({errors: error});
            this.setState({errorMessage: null});
        }
    },
    onSubmit: function() {
        Actions.loginSubmit({
            username: this.state.username,
            password: this.state.password
        });
    },
    handleChange: function(event){
        var newState = {};
        newState[event.target.name] = event.target.value;
        this.setState(newState);
    },
    render: function() {
        return (
            <form className="col-md-6">
              <ErrorBanner errorMessage={this.state.errorMessage} />
              <ValidationErrors errors={this.state.errors}/>
              <div className="form-group">
                <label htmlFor="Username">Username</label>
                <input type="username" onChange={this.handleChange} name="username" value={this.state.username} className="form-control" id="Username" placeholder="Username"/>
              </div>
              <div className="form-group">
                <label htmlFor="inputPassword">Password</label>
                <input type="password" onChange={this.handleChange} name="password" value={this.state.password} className="form-control" id="inputPassword" placeholder="Password"/>
              </div>
              <button type="button" onClick={this.onSubmit} className="btn btn-default">Submit</button>
            </form>
        );
    }
});