<?php use_stylesheet('/opBWTPlugin/css/styles.css', 'last') ?>
<?php use_javascript('https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization&language=ja', 'last') ?>
<?php use_javascript('/opBWTPlugin/js/d3.v3.min.js', 'last') ?>

<div id="bwt_rec_map_<?php echo $gadget->id ?>" class="dparts"><div class="parts">
	<div class='partsHeading'>
		<h3>雰囲気マップ</h3>
	</div>
	<div class="radio_mnt">
		<label><input style="display:inline-block;" type="radio" checked="checked" name="mental" value="att">attention</label>
		<label><input style="display:inline-block;" type="radio" name="mental" value="med">meditation</label>
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

function __getNode(selector){
	var node = $("#bwt_rec_map_<?php echo $gadget->id ?>");
	if(selector){
		return node.find(selector);
	}
	return node;
}
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

function __weigh_by_attention_org(itm){
	return 200 * (itm.mental.att / (itm.mental.att + itm.mental.med));
}
function __weigh_by_meditation_org(itm){
	return 200 * (itm.mental.med / (itm.mental.att + itm.mental.med));
}
var __mean_att_med = null;
var __stdev_att_med = null;

function __weigh_by_attention(itm){
	var ret = Math.max(0, 100 * (itm.rt_att_med - __mean_att_med) / __stdev_att_med);
	return ret;
}

function __weigh_by_meditation(itm){
	var ret = Math.max(0, -100 * (itm.rt_att_med - __mean_att_med) / __stdev_att_med);
	return ret;
}

var __weightfunc_map = {
	"att": __weigh_by_attention,
	"med": __weigh_by_meditation
};

var __items = [];
var __weightfunc = __weigh_by_attention;

var __map = null;

var __heatmap = null;

function __updateMap(items, weightfunc, fitbounds){
	__items = items || __items;
	__weightfunc = weightfunc || __weightfunc;

	var bounds = new google.maps.LatLngBounds();
	if(fitbounds){
		if(__cur_location){
			bounds.extend(__cur_location);
		}
	}
	__mean_att_med = d3.mean(__items, function(d){
		return d.mental ? d.mental.att / d.mental.med: null;
	});

	__stdev_att_med = 0;
	var stdev_num = 0;
	var mapdata = $(__items).map(function(){
		if(this.loc && this.mental){
			var pos = new google.maps.LatLng(this.loc.lat, this.loc.lng);
			if(fitbounds){
				bounds.extend(pos);
			}
			this.rt_att_med = this.mental.att / this.mental.med;
			__stdev_att_med += Math.pow(this.rt_att_med - __mean_att_med, 2);
			stdev_num++;
			return {
				"location": pos,
				"weight": __weightfunc(this)
			};
		}else{
			return null;
		}
	});
	__stdev_att_med = (0 == __stdev_att_med ? 1: Math.sqrt(__stdev_att_med / (stdev_num - 2)));
	if(fitbounds){
		__map.fitBounds(bounds);
	}
	if(__heatmap){
		__heatmap.setMap(null);
		__heatmap = null;
	}
	__heatmap = new google.maps.visualization.HeatmapLayer({
		data: mapdata
	});
	__heatmap.setMap(__map);

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
		url: openpne.apiBase + "bwpub/search",//filter with mine by default.
		dataType:"json",
		data:{
			apiKey: openpne.apiKey
		},
		success: function(result){
			if(result.count){
				$("input[name='mental']:radio").change(function(){
					__updateMap(result.items, __weightfunc_map[$(this).val()]);
				});
				__updateMap(result.items, null, true);
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
