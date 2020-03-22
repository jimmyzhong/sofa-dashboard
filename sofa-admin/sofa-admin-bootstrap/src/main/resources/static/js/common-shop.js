$(function () {
    console.log("shop-common-js=load==>end")
})





var SHOP_COMMON={
    ajax:{
       //formdata图片上传的参数，sucBack成功的回调，
        uploadImg:function (formdata,sucBack) {
            $.modal.loading("图片上传中");
            $.ajax({
                url: ctx + "ext/shop/oss/upload",
                data: formdata,
                type: "post",
                processData: false,
                contentType: false,
                success: function (result) {
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                    	sucBack(result)
                    } else {
                    	$.modal.alertError(result.msg);
                    }
                },
                error:function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("图片上传失败"+err.status);
                }
            })
        },
        get:function (url,data,sucBack) {
            $.modal.loading("数据获取中");
            $.ajax({
                url: url,
                type: "get",
                success: function (result){
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        sucBack(result)
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error:function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("数据请求失败"+err.status);
                }
            })
        },
        post:function (url,data,sucBack) {
            $.modal.loading("数据获取中");
            $.ajax({
                url: url,
                type: "post",
                data:data,
                processData: false,
                contentType: false,
                success: function (result){
                    $.modal.closeLoading();
                    if (result.code == web_status.SUCCESS) {
                        sucBack(result)
                    } else {
                        $.modal.alertError(result.msg);
                    }
                },
                error:function (err) {
                    $.modal.closeLoading();
                    $.modal.alertError("数据请求失败"+err.status);
                }
            })
        },
    }
}

