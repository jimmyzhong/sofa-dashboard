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
        }
    }
}

