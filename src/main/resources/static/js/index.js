$(function(){

    $("#publishModal").modal("hide");
	//$("#publishBtn").click(publish);
    $("#publishBtn").click(function () {

        var title=$("#recipient-name").val();
        var content=$("#message-text").val();

        $.ajax({
            type: "POST",
            url: CONTEXT_PATH+"/discuss/add",
            data: {"title":title,"content":content},
            success: function(data){
                data = $.parseJSON(data);

               // $("#hintBody").text(data.msg);//均可
                $("#hintBody").html(data.msg);
                $("#hintModal").modal("show");

                setTimeout(function(){
                    $("#hintModal").modal("hide");
                        // 刷新页面
                        if(data.code == 0) {
                            window.location.reload();
                        }

                }, 2000);
            }
        });


    })
});

function publish() {//二者均可
    $("#publishModal").modal("hide");

    // 获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();
    // 发送异步请求(POST)
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title":title,"content":content},
        function(data) {
            data = $.parseJSON(data);
            // 在提示框中显示返回消息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后,自动隐藏提示框
            setTimeout(function(){
                $("#hintModal").modal("hide");
                // 刷新页面
                if(data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    );
}