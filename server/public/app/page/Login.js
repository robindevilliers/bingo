
var Login = React.createClass({
    mixins: [Reflux.ListenerMixin],
    getInitialState : function(){
        return {
            username: '',
            password: '',
        };
    },
    componentDidMount: function() {
        this.listenTo(Actions.loginSubmit.failed, this.onLoginFail);
    },
    onLoginFail: function(error) {
        console.log('login failed')
        console.log(error)
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