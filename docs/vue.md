## hello
<div id="main">
	hello {{ msg }}
	<div class="layout">
		how are you ??
	</div>
</div>

<script>
  new Vue({
    el: '#main',
    data: { msg: 'Vue' }
  })
</script>

<style>
.layout {
 
}
</style>