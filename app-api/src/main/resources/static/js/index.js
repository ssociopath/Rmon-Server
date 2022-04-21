type = 'watch';

$(function () {
    if($.cookie("account")==='null'){
        $(location).attr("href", "login");
    }

    connectWebsocket($.cookie("account"));
    $("#watch_wrap").hide();

    $("#btn-res").click(function (e) {
        let msg = { type: "RES", from:$.cookie("account"), to:$.cookie("mac"),content: $(".form-control").val()};
        console.log(JSON.stringify(msg));
        ws.send(JSON.stringify(msg));
        e.preventDefault();
    });

    $("#btn-send").click(function (e) {
        let mac = $("#mac").val();
        let msg = { type: "CONNECT", from:$.cookie("account"), to:mac,
            content: JSON.stringify({ account:$.cookie("account"), mac}) };
        console.log(JSON.stringify(msg));
        ws.send(JSON.stringify(msg));
        e.preventDefault();
    });

    $("#btn-cmd").click(function (e) {
        let msg = { type: "CMD", from:$.cookie("account"), to:$.cookie("mac"),content: $("input[name='instructions']:checked").val()};
        console.log(JSON.stringify(msg));
        ws.send(JSON.stringify(msg));
        e.preventDefault();
    });

    $("#btn-control").click(function (e) {
        $("#watch_wrap").hide();
        $.getScript("js/listen.js");
        e.preventDefault();
    });

    $("#btn-img").click(function (e) {
        downloadFileByBase64($('#watch-img')[0].src);
        e.preventDefault();
    });

    $("#btn-task").click(function (e) {
        downloadExcel();
        e.preventDefault();
    });
});


// $(window).unload(function () {
//     $.cookie("account",null);
//     $.cookie("mac", null);
// });
