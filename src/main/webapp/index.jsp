<%@ page language="java"  contentType="text/html; charset=UTF-8" %>

<html>
<body>
<h2>Hello World!</h2>



springmvc上传文件
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="springmvc上传文件" />
</form>


富文本图片上传文件
<form name="form2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="富文本图片上传文件" />
</form>


<div id="editor">
    <p>欢迎使用 <b>wangEditor</b> 富文本编辑器</p>
</div>

<!-- 注意， 只需要引用 JS，无需引用任何 CSS ！！！-->
<script src="//unpkg.com/wangeditor/release/wangEditor.min.js"></script>
<script type="text/javascript" src="/wangEditor.min.js"></script>
<script type="text/javascript">
    var E = window.wangEditor
    var editor = new E('#editor')
    // 或者 var editor = new E( document.getElementById('editor') )
    editor.customConfig.uploadImgServer = '/manage/product/richtext_img_upload.do';
    editor.customConfig.uploadFileName = 'upload_file';
    editor.customConfig.uploadImgHeaders = {
        'Accept' : 'multipart/form-data'
    };
    editor.customConfig.debug = true;
    editor.customConfig.uploadImgMaxSize = 10 * 1024 * 1024;
    editor.customConfig.uploadImgHooks = {
        error: function (xhr, editor) {
            alert("2:"+xhr);
            // 图片上传出错时触发
            // xhr 是 XMLHttpRequst 对象，editor 是编辑器对象
        },
        fail: function (xhr, editor, result) {
            alert("1:"+xhr);

        },
    };
    editor.create();
</script>

</body>
</html>
