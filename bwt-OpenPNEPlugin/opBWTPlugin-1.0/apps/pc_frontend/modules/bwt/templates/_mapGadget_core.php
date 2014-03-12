<script id="tmpl_infopanel" type="text/x-jquery-tmpl">
<div>
<div class="infoWindow" style="width:100%;white-space:nowrap; overflow:hidden;">
	<div>
		${titleStr}
	</div>
	<div>
		{{if imageUrl}}
		<img src="${imageUrl}" style="height:15em;"/>
		{{/if}}
		<span>${commentStr}</span>
	</div>
</div>
</div>
</script>
<script type="text/javascript">
$(document).ready(function(){
var pne_current_user = {
	name: "<?php echo $u_name ?>",
	home: "<?php echo $u_homeLocation ?>"
};
var trgId = "bwt_map_frame_"+ "<?php echo $gadget->id ?>";

var pne_reqparams = {
	"extract": true
};

var _bounds = new google.maps.LatLngBounds();

var _fitBounds = function(map, bounds){
	if(!map.__fitBounds_inflight){
		map.__fitBounds_inflight = true;
		setTimeout(function(){
			map.__fitBounds_inflight = false;
			map.fitBounds(bounds);
			if(map.getZoom() > 13){
				map.setZoom(13);
			}
		}, 1000);
	}
}

var __infoWindow = new google.maps.InfoWindow;
var __showInfoWindow = function(map, marker, content){
	__infoWindow.setContent(content);
	__infoWindow.open(map, marker);
};
var __hideInfoWindow = function(){
	__infoWindow.close();
};
var __addMarker = function(itm, map){
	itm.titleStr = itm.member.name + "さんが" + moment(1*itm.epoc).calendar() + "にチェックイン";
	itm.commentStr = (itm.comment || "コメントなし");
	itm.imageUrl = (itm.ext && itm.ext.data && itm.ext.data.image_url ? itm.ext.data.image_url[0] : undefined);
	var content = $("#tmpl_infopanel").tmpl(itm);
	var s_a = itm.mental.att;
	var s_m = itm.mental.med;
	var hsl = d3.hsl((s_a * 360  + s_m * 240)/(s_a + s_m), 1, (itm.mental.att + itm.mental.med) / 200);
	var rgb = hsl.rgb();

	var marker = new google.maps.Marker({
		map:map,
//		draggable:true,
		icon: {
			path: google.maps.SymbolPath.CIRCLE,
			scale: 10,
			strokeWeight: 2,
			strokeColor:"#544a47",
			strokeOpacity: 1,
			fillOpacity: .7,
			fillColor:["rgb(",rgb.r,",",rgb.g,",",rgb.b,")"].join("")
		},
		animation: google.maps.Animation.DROP,
		position: new google.maps.LatLng(itm.loc.lat, itm.loc.lng),
		title: itm.titleStr
	});
	google.maps.event.addListener(marker, "click", function(e){
		__showInfoWindow(map, marker, content.html());
	});
	_bounds.extend(marker.getPosition());
	_fitBounds(map, _bounds);
};
var hongou = new google.maps.LatLng(35.713427,139.762308);

var start = function(){
	var geocoder = new google.maps.Geocoder();
	geocoder.geocode({
		"address": pne_current_user.home,
		"region": "jp"
	}, function(results, status){
		initMap(document.getElementById(trgId)
			, (status == google.maps.GeocoderStatus.OK) ? results[0].geometry.location : hongou);
	});
};
var initMap = function (trg, center){
	var map = new google.maps.Map(trg,{
		zoom:13,
		center: center,
		mapTypeId:google.maps.MapTypeId.ROADMAP
	});
	$.ajax({
		type: "GET",
		url: openpne.apiBase + "bwdata/search",//filter with mine by default.
		dataType:"json",
		data:{
			<?php if (!empty($scope)):?>
			"scope":"<?php echo $scope;?>",
			<?php endif ?>
			"apiKey": openpne.apiKey,
			"extrct":true
		},
		success: function(result){
			if(result.count){
				var delay = 0;
				$(result.items).each(function(){
					var tmp = this;
					setTimeout(function(){
						__addMarker(tmp, map);
					},(delay += 300));
				});
			}
		},
		error: function(xhr, status, e){
		}
	});

	google.maps.event.addListener(map, "click", function(e){
		__hideInfoWindow();
	});
};

start();

});
</script>
