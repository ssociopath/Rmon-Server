type = 'control';


//计算control-img的宽和高变量
var imageWidth = $("#control").innerWidth();
var imageHeight = $("#control").innerHeight();

//当浏览器大小变化时，更新control-img的宽和高变量
$(window).resize( function  () {
    imageWidth = $("#control").innerWidth();
    imageHeight = $("#control").innerHeight();
});

//Jquery禁用网页右键菜单
$(document).bind("contextmenu",function(e){
    return false;
});

//键盘被按下去事件
$(document).keydown(function (event) {
    let obj = {};
    obj.openType = "keydown";
    obj.keyCode = event.keyCode;
    let msg = { type: "CONTROL", from:$.cookie("account"), to:$.cookie("mac"),content: JSON.stringify(obj)};
    ws.send(JSON.stringify(msg));

    //禁用一些快捷键
    if (event.ctrlKey && window.keyCode===65){ //禁用ctrl + a 功能
        return false;
    }

    //禁用一些快捷键
    if (event.ctrlKey && window.keyCode===67){	//禁用ctrl + c 功能
        return false;
    }

    //禁用一些快捷键
    if (event.ctrlKey && window.keyCode===83){	//禁用ctrl + s 功能
        return false;
    }

    //禁用一些快捷键
    if (event.ctrlKey && window.keyCode===86){	//禁用ctrl + v 功能
        return false;
    }

    //你想禁用其他快捷键时
    //console.log(event);
    //比如说我按下 A键 keyCode=65 获取到keyCode，然后按以上的方法禁止
    //目前F1到F12还没有禁用，如果需要的话可以加上。

});

//键盘被弹起来事件
$(document).keyup(function (event) {
    let obj = {};
    obj.openType = "keyup";
    obj.keyCode = event.keyCode;
    let msg = { type: "CMD", from:$.cookie("account"), to:$.cookie("mac"),content: JSON.stringify(obj)};
    ws.send(JSON.stringify(msg));
    event.preventDefault();
});

//鼠标按钮被按下
$(document).mousedown(function (event) {
    let obj = {};
    obj.openType = "mousedown";
    obj.button = event.button;
    obj.clientX = event.clientX; //需要在后台重新计算转换成远程桌面上的真实的坐标
    obj.clientY = event.clientY; //需要在后台重新计算转换成远程桌面上的真实的坐标
    obj.imageWidth = imageWidth;
    obj.imageHeight = imageHeight;
    let msg = { type: "CMD", from:$.cookie("account"), to:$.cookie("mac"),content: JSON.stringify(obj)};
    ws.send(JSON.stringify(msg));
    event.preventDefault();
});

//鼠标按钮被松开
$(document).mouseup(function (event) {
    let obj = {};
    obj.openType = "mouseup";
    obj.button = event.button;
    obj.clientX = event.clientX;
    obj.clientY = event.clientY;
    obj.imageWidth = imageWidth;//当前浏览器下image标签占用的宽和高,传这两个值到后台用于修正真实的点击的x和y坐标
    obj.imageHeight = imageHeight;
    let msg = { type: "CMD", from:$.cookie("account"), to:$.cookie("mac"),content: JSON.stringify(obj)};
    ws.send(JSON.stringify(msg));
    event.preventDefault();
});


