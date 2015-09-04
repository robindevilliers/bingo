

var Register = React.createClass({
    mixins: [Reflux.ListenerMixin],
    getInitialState : function(){
        return {
            emailAddress: '',
            username: '',
            password1: '',
            password2: ''
        };
    },
    componentDidMount: function() {
        this.listenTo(Actions.registerSubmit.failed, this.onRegisterFail);
    },
    onRegisterFail: function(error) {
        console.log('register failed')
        console.log(error)
    },
    onSubmit: function() {
        Actions.registerSubmit({
            emailAddress: this.state.emailAddress,
            username: this.state.username,
            password: this.state.password1
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
                <label for="exampleInputEmail">Email address</label>
                <input type="email" onChange={this.handleChange} name="emailAddress" value={this.state.emailAddress} className="form-control" id="exampleInputEmail" placeholder="Email"/>
              </div>
              <div className="form-group">
                <label for="Username">Username</label>
                <input type="username" onChange={this.handleChange} name="username" value={this.state.username} className="form-control" id="Username" placeholder="Username"/>
              </div>
              <div className="form-group">
                <label for="exampleInputPassword1">Password</label>
                <input type="password" onChange={this.handleChange} name="password1" value={this.state.password1} className="form-control" id="exampleInputPassword1" placeholder="Password"/>
              </div>
              <div className="form-group">
                  <label for="exampleInputPassword2">Re-enter password</label>
                  <input type="password" onChange={this.handleChange}  name="password2" value={this.state.password2} className="form-control" id="exampleInputPassword2" placeholder="Password"/>
              </div>
              <button type="button" onClick={this.onSubmit} className="btn btn-default">Submit</button>
            </form>
        );
    }
});



