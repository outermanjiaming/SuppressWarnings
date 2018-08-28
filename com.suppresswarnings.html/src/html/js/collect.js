function newreply(myimg, reply, replyId) {
	var li = "<li class='boder_v1'><img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='"+myimg+"'><span>"+reply+"</span><div class='form-group text-center'><button type='button' class='btn btn-xs' onclick='changesame(this)'>同义句</button> - <button type='button' class='btn btn-xs' onclick='changereply(this)'>回复</button><div class='sendreplydiv hidden' style='padding-top:15px;'><input type='text' class='input btn-xs sendreplyinput' placeholder='请输入回复内容' size='30'><button type='button' data-replyid='"+replyId+"' data-which='replysame' class='btn btn-xs sendreplybtn' onclick='sendreply(this)'>发送</button></div></div></li>"
	$("#business").append(li)
}
function changesame(obj) {
	var sendreplydiv = $(obj).siblings(".sendreplydiv")[0]
	$(sendreplydiv).show()
	var sendreplybtn = $(sendreplydiv).children(".sendreplybtn")[0]
	$(sendreplybtn).data("which", "same")
}
function changereply(obj) {
	var sendreplydiv = $(obj).siblings(".sendreplydiv")[0]
	$(sendreplydiv).show()
	var sendreplybtn = $(sendreplydiv).children(".sendreplybtn")[0]
	$(sendreplybtn).data("which", "reply")
}
function sendreply(obj) {
	var replyId = $(obj).data("replyid")
	var which = $(obj).data("which")
	console.log(replyId + " & " + which)
	var sendreplyinput = $(obj).siblings(".sendreplyinput")[0]
	var reply = $(sendreplyinput).val()
	alert(reply)
	$(sendreplyinput).val("")
}
function replyquiz(obj) {
	var quizId = $(obj).data("quizid")
	var reply = $("#replyquiz").val()
	var myimg = $("#userimg").val()
	var myname= $("#username").val()
	jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "replyquiz",
	    random : randnum,
	    ticket : ticket,
	    state : state,
	    reply : reply
    },
    success: function( result ) {
    	if("fail" == result) {
    		$("#replyquiz").attr("placeholder","提交失败，稍后重试")
    	} else {
    		$("#replyquiz").attr("placeholder","提交成功，可以继续提交")
    		newreply(myimg, reply, state)
    	}
    	$("#replyquiz").val("")
    	$("#replyquiz").focus()
    },
    error: function( xhr, result, obj ) {
    	$("#replyquiz").attr("placeholder","服务端错误，请按要求输入重试")
    }
  })
}
jQuery.ajax({
    url: "/wx.http?r=" + Math.random(),
    data: {
	    action : "collect",
	    random : randnum,
	    ticket : ticket,
	    state : state
    },
    success: function( result ) {
      if("fail" == result) {
        console.log('fail to access_token: ' + result)
        $('#inviteTitle').text('素朴网联!')
        oauth2()
      } else {
        $('#inviteTitle').text('素朴网联')
        
        var collect = JSON.parse(result)
        var arr = collect.array
        var length = arr.length
        for (var k = 0; k < length; k++) {
        	$("#crewimg").append("<img style='width: 20px;height: 20px;margin-right: 5px;margin-top:2px;' src='" + arr[k] + "'/>");
        }
        
        var myimg = collect.userimg
        var myname = collect.username
        $("#business > .boder_v1").append("<div class='form-group text-center'><input id='replyquiz' type='text' class='input btn-xs' placeholder='请按要求输入回复内容' size='33'><button type='button' class='btn btn-xs' data-quizid='"+state+"' onclick='replyquiz(this)'>回复</button></div>")
        $("#business").append("<input id='userimg' type='text' class='sr-only hidden' value='"+collect.userimg+"'/>")
        $("#business").append("<input id='username' type='text' class='sr-only hidden' value='"+collect.username+"'/>")
      }
    },
    error: function( xhr, result, obj ) {
      console.log("[lijiaming] collect err: " + result)
      $('#inviteTitle').text('!素朴网联')
      oauth2()
    }
})
