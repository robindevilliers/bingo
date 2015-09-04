var LandingPage = React.createClass({

    getInitialState: function() {
        return {view: 'menu'};
    },
    clickRegister: function(event){
        this.setState({view: 'register'});
    },
    clickLogin: function(event){
        this.setState({view: 'login'});
    },
    render: function() {

        var pane = null;
        switch(this.state.view){
            case 'menu':
                pane = (
                    <div className="row">
                        <div className="col-md-1">
                           <button onClick={this.clickRegister}  className="btn btn-success">Register</button>
                        </div>
                        <div className="col-md-1">
                           <button onClick={this.clickLogin}  className="btn btn-success">Login</button>
                        </div>
                    </div>
                );
                break;
            case 'register':
                pane = (
                    <Register/>
                );
                break;
            case 'login':
                pane = (
                    <Login/>
                );
                break;

        }


        return (
            <div className="container" >
                <div className="row">
                    <div className="col-md-3">
                        <img src='/images/logo.jpg'></img>
                    </div>
                    <div className="col-md-6">
                        <div className="container">
                            <div className="row">
                                <div className="col-md-12">
                                    <h1>Welcome to Mad Hat Bingo</h1>
                                </div>
                            </div>
                        </div>
                        <div className="container">
                            {pane}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});


