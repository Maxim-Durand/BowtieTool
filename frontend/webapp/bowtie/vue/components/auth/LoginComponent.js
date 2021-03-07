/**
 * LoginComponent
 * Handles the login process
 * Related template: common/authentication.html
 */

let LoginComponent =  {
    template: '#login-template',
    props: {
        cleanErrorMessages: Function
    },
    data: function() {
        return {
            user: {
                email: '',
                password: '',
                twoFactorAuth: false,
                totpToken: null,
                id: null,
                loginToken: null
            },
            errors: {
                InvalidCredentialsErr: {
                    message: 'Invalid credentials provided.',
                    show: false
                },
                InvalidEmailErr: {
                    message: 'Valid email is required.',
                    show: false
                },
                InvalidTotpCodeErr: {
                    message: '6-digit code is required.',
                    show: false
                },
                ExpiredTotpTokenErr: {
                    message: 'This code has expired, try with a new one.',
                    show: false
                }
            }
        }
    },
    methods: {
        // Checks if the login form is valid
        checkLoginForm: function() {
            this.cleanErrorMessages(this.errors);
            let isValid = true;
            if (!this.validEmail()) {
                this.errors.InvalidEmailErr.show = true;
                this.user.email = '';
                isValid = false;
            }
            if (this.user.totpToken !== null && !this.validTotpCode()) {
                this.errors.InvalidTotpCodeErr.show = true;
                isValid = false;
            }
            return isValid;
        },
        // Submits the login form
        submitLoginForm: function () {
            if (this.checkLoginForm()) {
                if (this.user.twoFactorAuth) {
                    let params = JSON.stringify({ "token_totp": this.user.totpToken});
                    let url = window.LOGIN_2FA + this.user.id + '/' + this.user.loginToken;
                    axios.post(url, params, {
                        headers: {
                            'Content-type': 'application/json'
                        }
                    })
                        .then(res => {
                            this.processToken(res.data.token);
                        })
                        .catch(error => {
                            if (error.response) this.filter2faLoginErrors(error.response);
                        })
                } else {
                    let params = JSON.stringify({"email": this.user.email, "password": this.user.password});
                    axios.post(window.LOGIN, params, {
                        headers: {
                            'Content-type': 'application/json'
                        },
                    })
                        .then(res => {
                            this.setLoginMode(res.data);
                        })
                        .catch(error => {
                            if (error.response) this.filterLoginErrors(error.response);
                        })
                }
            }

        },
        // Checks if the mail matches the right pattern
        validEmail: function() {
            let mailRegex = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            return mailRegex.test(this.user.email);
        },
        validTotpCode: function() {
            let totpCodeRegx = /^[0-9]{6}$/;
            return totpCodeRegx.test(this.user.totpToken);
        },
        setLoginMode: function(data) {
            if (data.uidb64 !== undefined && data.token !== undefined) {
                this.user.id = data.uidb64;
                this.user.loginToken = data.token;
                this.user.twoFactorAuth = true;
                this.user.totpToken = '';
            } else {
                this.processToken(data.token);
            }
        },

        // Handles the received token if the login form has been successfully submitted
        processToken: function (token) {
            localStorage.setItem('token', token);
            axios.get(window.USER_INFO, {
                headers: {
                    'Authorization': 'Token ' + token
                }
            })
                .then(res => {
                    this.processName(res.data.username);
                })

        },
        // Handles the user information received thanks to the token
        processName: function(name) {
            localStorage.setItem('username', name);
            window.location.assign(window.BASE_PATH);
        },
        // Handles http errors coming from the login form submission
        filterLoginErrors: function(error) {
            if (error.status === 401 || error.status === 400) {
                this.errors.InvalidCredentialsErr.show = true;
            } else {
                console.log('Unexpected error while logging in');
            }
        },
        filter2faLoginErrors: function(error) {
            switch(error.status) {
                case 400:
                    if(error.data.errors !== undefined) {
                        this.user.twoFactorAuth = false;
                        this.user.loginToken = null;
                        alert('Your login token has expired. Please try again.');
                    } else {
                        this.errors.ExpiredTotpTokenErr.show = true;
                        this.qrCode = '';
                    }
                    break;
                default:
                    console.log('Error while contacting the server.');
            }
        }
    }
}
