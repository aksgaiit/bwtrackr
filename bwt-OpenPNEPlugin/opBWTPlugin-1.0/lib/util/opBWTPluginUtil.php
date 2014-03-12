<?php
/**
 * bwtrackr utility class
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */

class opBWTPluginUtil{
	private static $m = null;
	public static function getMapDatabase(){
		if(empty(self::$m)){
			$host = Doctrine::getTable("SnsConfig")->get("op_bwt_plugin_mapdb_host", "localhost");
			$port = Doctrine::getTable("SnsConfig")->get("op_bwt_plugin_mapdb_port", 27017);
			$name = Doctrine::getTable("SnsConfig")->get("op_bwt_plugin_mapdb_name", "bwtdata");
			$m = new MongoClient("mongodb://".$host.":".(strval($port)));
		}
		return $m->selectDB($name);
	}
}

