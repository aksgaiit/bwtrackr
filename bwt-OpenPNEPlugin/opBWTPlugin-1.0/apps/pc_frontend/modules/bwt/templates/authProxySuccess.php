<script type="text/javascript">
$(document).ready(function(){
	var __uinfo = {
		name: "<?php echo $member->name?>",
		id: "<?php echo $member->id?>",
		apiKey: openpne.apiKey,
		apiBase: openpne.apiBase
	};
window["pingCredential"] = function (){
	if("undefined" != typeof(window["__msgbroker"])){
		if(openpne){
			__msgbroker.onCredentialReady(JSON.stringify(__uinfo));
		}else{
			__msgbroker.onCredentialError(JSON.stringify({
				"msg": "no openpne instance found"
			}));
		}
	}else{
		alert(JSON.stringify(__uinfo));
	}
};
});

</script>
