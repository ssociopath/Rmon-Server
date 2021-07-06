var ws;
var task;

function connectWebsocket(account) {
    ws = new WebSocket("ws://bobooi.com:8081/watch/ws/" + account);

    // 连接成功
    ws.onopen = () => {
        console.log("连接服务端WebSocket成功");
    };

    // 监听服务端消息
    ws.onmessage = (msg) => {
        let message = JSON.parse(msg.data);
        if (message.type === "CONNECT") {
            if(message.content==='连接失败，请检查网络和被控端'){
                alert(message.content);
            }else{
                let rule = JSON.parse(msg.content);
                $.cookie('mac',rule.mac);
                $.cookie('account',rule.account);
                $("#status").html(`被控端：${rule.mac}`);
                $("#current-user").html(`当前用户：${rule.account}`);
                if(message.permission==='1'){
                    $("#permission").html(`当前权限：<span>允许访问</span>`);
                    $("input[name='instructions']").attr("disabled", "disabled");
                    $("#btn-cmd").attr("disabled", "disabled");
                }else{
                    $("#permission").html(`当前权限：<span>允许操作</span>`);
                }
                $("#watch_wrap").show();
                $("#input_wrap").hide();
            }
        }else if (message.type === "IMAGE") {
            $("#watch").html('<img id="watch-img" src="data:image/jpg;base64,' + message.content + '">');
        } else if (message.type === "TASK") {
            task = JSON.parse(message.content);
            createShowingTable(task);
        }
    };

    // 连接失败
    ws.onerror = () => {
        console.log("连接失败，正在重连...");
        connectWebsocket();
    };

    // 连接关闭
    ws.onclose = () => {
        console.log("连接关闭");
    };
}

function createShowingTable(data) {
    var tableStr = "";
    var len = data.length;
    for (var i = 0; i < len; i++) {
        tableStr = tableStr + "<tr><td>" + data[i].pid + "</td>" + "<td>" + data[i].name
            + "</td>" + "<td>" + data[i].user + "</td>" + "<td>" + data[i].mem + "</td>" + "<td>" + data[i].cpu + "</td></tr>";
    }
    $("#dataTable").html(tableStr);
}
