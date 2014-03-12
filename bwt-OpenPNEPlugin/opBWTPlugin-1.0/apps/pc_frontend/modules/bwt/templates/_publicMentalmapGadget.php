<?php use_stylesheet('/opBWTPlugin/css/styles.css', 'last') ?>
<?php use_javascript('https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization&language=ja', 'last') ?>
<?php use_javascript('/opBWTPlugin/js/d3.v3.min.js', 'last') ?>

<div id="bwt_mental_map_<?php echo $gadget->id ?>" class="dparts"><div class="parts">
	<div class='partsHeading'>
		<h3>メンタルマップ</h3>
	</div>
	<div class="radio_mnt">
		<label><input style="display:inline-block;" type="radio" checked="checked" name="mental" value="att">attention</label>
		<label><input style="display:inline-block;" type="radio" name="mental" value="med">meditation</label>
	</div>
	<select name="weigh" class="select_weigh">
		<option value="average">average</option>
		<option value="total">total</option>
		<option value="ratio">ratio</option>
		<option value="ratiostd">ratio/std.dev.</option>
	</select>
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
	var node = $("#bwt_mental_map_<?php echo $gadget->id ?>");
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

var __weightfunc_maps = {
	"average": {
		"att": function (itm){
			return itm.att.ave;
		},
		"med": function(itm){
			return itm.att.med;
		}
	},
	"total":{
		"att": function (itm){
			return itm.att.ave * itm.att.num;
		},
		"med": function(itm){
			return itm.att.med * itm.med.num;
		}
	},
	"ratio": {
		"att": function(itm){
			return 200 * (itm.att.ave / ((itm.att.ave + itm.med.ave) || 1));
		},
		"med": function(itm){
			return 200 * (itm.med.ave / ((itm.att.ave + itm.med.ave) || 1));
		}
	},
	"ratiostd":{
		"att": function(itm){
			return Math.max(0, 100 * (itm.rt_att_med - __mean_att_med) / __stdev_att_med);
		},
		"med": function(itm){
			return Math.max(0, -100 * (itm.rt_att_med - __mean_att_med) / __stdev_att_med);
		}
	}
};

var __items = [];
var __weightfunc_map = __weightfunc_maps["average"];
var __weightfunc = __weightfunc_map["att"];

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
		return d.att && d.med ? d.att.ave / (d.med.ave || 1): null;
	});

	__stdev_att_med = 0;
	var stdev_num = 0;
	var mapdata = $(__items).map(function(){
		if(this.loc && this.att && this.med){
			var pos = new google.maps.LatLng(this.loc.lat, this.loc.lng);
			if(fitbounds){
				bounds.extend(pos);
			}
			this.rt_att_med = this.att.ave / (this.med.ave || 1);
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
		url: openpne.apiBase + "bwpub/searchMental",//filter with mine by default.
		dataType:"json",
		data:{
			apiKey: openpne.apiKey
		},
		success: function(result){
			if(result.count){
				$("input[name='mental']:radio").change(function(){
					__updateMap(result.items, __weightfunc_map[$(this).val()]);
				});
				$("select[name='weigh']").change(function(){
					__weightfunc_map = __weightfunc_maps[$(this).val()];
					__updateMap(result.items, __weightfunc_map[$("input[name='mental']:radio").val()]);
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
