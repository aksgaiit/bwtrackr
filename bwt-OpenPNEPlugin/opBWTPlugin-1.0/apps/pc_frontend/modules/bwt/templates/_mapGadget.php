<?php use_stylesheet('/opBWTPlugin/css/styles.css', 'last') ?>
<?php use_javascript('/opBWTPlugin/js/d3.v3.min.js', 'last') ?>
<?php use_javascript('https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization&language=ja', 'last') ?>
<?php use_javascript('/opBWTPlugin/js/moment.min.js', 'last') ?>
<?php use_javascript('/opBWTPlugin/js/lang/ja.js', 'last') ?>
<?php
	$scope="friend";
?>

<div id="bwt_rec_map_<?php echo $gadget->id ?>" class="dparts"><div class="parts">
	<div class='partsHeading'>
		<h3><?php echo $u_name?>さん、友人の記録した場所</h3>
	</div>
	<div class='block'>
		<div class='gmap' style='width:100%;height:30em;border-radius:6px;' id='bwt_map_frame_<?php echo $gadget->id ?>'>
		</div>
	</div>
</div></div>
<?php include("_mapGadget_core.php");?>
