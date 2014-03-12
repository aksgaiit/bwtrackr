<?php use_stylesheet('/opBWTPlugin/css/styles.css', 'last') ?>
<?php use_javascript('https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization&language=ja', 'last') ?>
<div id="bwt_rec_map_<?php echo $gadget->id ?>" class="dparts"><div class="parts">
	<div class='partsHeading'>
		<h3>脳波サーモマップ</h3>
	</div>
	<div class='block'>
		<div class='gmap' style='width:100%;height:30em;border-radius:6px;' id='bwt_map_frame_<?php echo $gadget->id ?>'>
		</div>
	</div>
</div></div>
<script type="text/javascript">
$(document).ready(function(){
var pne_current_user = {
	name: "<?php echo $u_name ?>",
	home: "<?php echo $u_homeLocation ?>"
};
var trgId = "bwt_map_frame_"+ "<?php echo $gadget->id ?>";

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
}
var __hideInfoWindow = function(){
	__infoWindow.close();
}
var hongou = new google.maps.LatLng(35.713427,139.762308);

var __cur_location = null;

var start = function(){
	var geocoder = new google.maps.Geocoder();
	geocoder.geocode({
		"address": pne_current_user.home,
		"region": "jp"
	}, function(results, status){
		initMap(document.getElementById(trgId)
			, (status == google.maps.GeocoderStatus.OK) ? results[0].geometry.location : hongou);
	});
}

function __weigh_by_attention(itm){
	return 1 * itm.mental.att;
}
var __items = [];
var __weightfunc = __weigh_by_attention;

var __map = null;

function __updateMap(items, weightfunc){
	__items = items || __items;
	__weightfunc = weightfunc || __weightfunc;

	var bounds = new google.maps.LatLngBounds();
	if(__cur_location){
		bounds.extend(__cur_location);
	}
	var mapdata = $(__items).map(function(){
		var pos = new google.maps.LatLng(this.loc.lat, this.loc.lng);
		bounds.extend(pos);
		return {
			"location": pos,
			"weight": __weightfunc(this)
		};
	});
	__map.fitBounds(bounds);
	var heatmap = new google.maps.visualization.HeatmapLayer({
		data: mapdata
	});
	heatmap.setMap(__map);

//	if(__map.getZoom() > 13){
//		__map.setZoom(13);
//	}
}

var initMap = function (trg, center){
	__cur_location = center;
	__map = new google.maps.Map(trg,{
		zoom:13,
		center: center,
		mapTypeId:google.maps.MapTypeId.ROADMAP
	});
	$.ajax({
		type: "GET",
		url: openpne.apiBase + "bwdata/search",//filter with mine by default.
		dataType:"json",
		data:{
			apiKey: openpne.apiKey
		},
		success: function(result){
			if(result.count){
				__updateMap(result.items);
			}
		},
		error: function(xhr, status, e){
		}
	});

	google.maps.event.addListener(__map, "click", function(e){
		__hideInfoWindow();
	});
};

start();

});
</script>
