
function login() {
    $.ajax({
        type: 'POST',
        url: '/user/login',
        dataType:'json',
        data:{
            userName: $("#login-name").val(),
            password: $("#login-pass").val()
        },
        success : function (data) {
            if (data.status === 200){
                window.location.href = "/page/chatRoom" ;
                connect(data.msg);
            }else {
                alert(data.msg);
            }
        }
    })
}

