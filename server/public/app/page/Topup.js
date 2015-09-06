
var Topup = React.createClass({
    mixins: [Reflux.ListenerMixin],
    getInitialState : function(){
        return {
            mode: '',
            amount: ''
        };
    },
    componentDidMount: function() {
        this.listenTo(Actions.topupSubmit.failed, this.onTopupFail);
        this.listenTo(Actions.topupSubmit.success, this.onTopupSuccess);
    },
    onTopupSuccess: function(){
        this.setState({mode: 'topupSuccessful'});
    },
    onTopupFail: function(error) {
        console.log('topup failed')
        console.log(error)
    },
    onSubmit: function() {
        Actions.topupSubmit({
            amount: this.state.amount
        });
    },
    onLobby: function(){
        Actions.displayLobby();
    },
    handleChange: function(event){
        var newState = {};
        newState[event.target.name] = event.target.value;
        console.log(event.target.value);
        this.setState(newState);
    },
    render: function() {
        if (this.state.mode == 'topupSuccessful'){
            return (
            <div>
                <div className="jumbotron well">
                  <h3>Topup successful</h3>
                  <p>Thanks for topping up your account.</p>
                  <p>Have a great time.</p>

                </div>
                <div className="col-md-2">
                    <button type="button" onClick={this.onLobby} className="btn btn-default">Lobby</button>
                </div>
            </div>
            );
        }

        return (
            <form className="col-md-8">
              <div className="panel panel-default well">
                <div className="panel-heading">
                  <h3 className="panel-title">Please select how much you wish to top up.</h3>
                </div>
                <div className="panel-body">

                    <div className="radio">
                      <label><input type="radio" onChange={this.handleChange} name="amount" value="5" />Five Pounds</label>
                    </div>
                    <div className="radio">
                      <label><input type="radio" onChange={this.handleChange} name="amount" value="10" />Ten Pounds</label>
                    </div>

                    <div className="radio">
                      <label><input type="radio"onChange={this.handleChange} name="amount" value="20" />Twenty Pounds</label>
                    </div>
                    <div className="radio">
                      <label><input type="radio" onChange={this.handleChange} name="amount" value="50" />Fifty Pounds</label>
                    </div>
                </div>
              </div>
              <div className="row">
              <div className="col-md-2 pull-right">
                <button type="button" onClick={this.onSubmit} className="btn btn-default">Submit</button>
              </div>
              <div className="col-md-2 pull-right">
                <button type="button" onClick={this.onLobby} className="btn btn-default">Cancel</button>
              </div>
              </div>
            </form>
        );
    }
});