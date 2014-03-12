<?php
class opBWTPluginConfigurationForm extends BaseForm{
	public function configure(){
		$this->setWidget("mapdb_host", new sfWidgetFormInput());
		$this->setDefault("mapdb_host", Doctrine::getTable("SnsConfig")->get("op_bwt_plugin_mapdb_host", "localhost"));
		$this->setValidator("mapdb_host", new sfValidatorString(array("trim" => true)));
		$this->widgetSchema->setLabel("mapdb_host", "Map database host");
		$this->widgetSchema->setHelp("mapdb_host", "Please input host name of map database");

		$this->setWidget("mapdb_port", new sfWidgetFormInput());
		$this->setDefault("mapdb_port", Doctrine::getTable("SnsConfig")->get("op_bwt_plugin_mapdb_port", "27017"));
		$this->setValidator("mapdb_port", new sfValidatorNumber(array("max" => 65535,"min"=>1)));
		$this->widgetSchema->setLabel("mapdb_port", "Map database port");
		$this->widgetSchema->setHelp("mapdb_port", "Please input port number of map database");

		$this->setWidget("mapdb_name", new sfWidgetFormInput());
		$this->setDefault("mapdb_name", Doctrine::getTable("SnsConfig")->get("op_bwt_plugin_mapdb_name", "bwtdata"));
		$this->setValidator("mapdb_name", new sfValidatorString(array("trim" => true)));
		$this->widgetSchema->setLabel("mapdb_name", "Map database name");
		$this->widgetSchema->setHelp("mapdb_name", "Please input host name of map database");

		$this->widgetSchema->setNameFormat("op_bwt_plugin[%s]");
	}
	public function save(){
		$names = array("mapdb_host", "mapdb_port", "mapdb_name");
		foreach($names as $name){
			if(!is_null($this->getValue($name))){
				Doctrine::getTable("SnsConfig")->set("op_bwt_plugin_".$name, $this->getValue($name));
			}
		}
	}
}
