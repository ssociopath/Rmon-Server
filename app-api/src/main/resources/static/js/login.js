$(function () {
    $("#login-form-link").click(function (e) {
        $("#login-form").delay(100).fadeIn(100);
        $("#register-form").fadeOut(100);
        $("#register-form-link").removeClass("active");
        $(this).addClass("active");
        e.preventDefault();
    });
    $("#register-form-link").click(function (e) {
        $("#register-form").delay(100).fadeIn(100);
        $("#login-form").fadeOut(100);
        $("#login-form-link").removeClass("active");
        $(this).addClass("active");
        e.preventDefault();
    });

    $("#login-submit").click(function () {
        let account = $("#login-username").val();
        let password = $("#login-password").val();
        $.post(
            "http://bobooi.com:8081/watch/user/login",
            {
                account,
                password
            },
            function (data, status) {
                console.log(data);
                if (data.code === "00000") {
                    console.log(data.data);
                    $.cookie("account", account);
                    $(location).attr("href", "index");
                } else {
                    alert(data.message);
                }
            }
        );
    });

    $("#register-submit").click(function () {
        let account = $("#username").val();
        let password = $("#password").val();
        let confirm_password = $("#confirm-password").val();
        if(password === confirm_password && password!==''){
            $.post(
                "http://bobooi.com:8081/watch/user/register",
                {
                    account,
                    password
                },
                function (data, status) {
                    console.log(data);
                    if (data.code === "00000") {
                        console.log(data.data);
                        alert("注册成功，请登录");
                        // $.cookie("account", account);
                        // $(location).attr("href", "index");
                    } else {
                        alert(data.message);
                    }
                }
            );
        }else{
            alert("填写正确信息！")
        }
    });
});
