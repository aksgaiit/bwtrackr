<?php
/**
 * diary components.
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */
class bwtComponents extends sfComponents{
	protected function setUserLocation(sfWebRequest $request){
		$member = $this->getUser()->getMember();
		$this->u_name = $member->getName();
		$profLoc = $member->getProfile("user_city");
		if(null == $profLoc){
			$profLoc =  $member->getProfile("op_preset_region");
		}
		if(null != $profLoc){
			$this->u_homeLocation =$profLoc->getValue(); 
		}
		$this->u_homeCaption = $this->u_homeLocation;
	}
	public function executeMapGadget(sfWebRequest $request){
		$this->setUserLocation($request);
	}
	public function executeMapGadget_my(sfWebRequest $request){
		$this->executeMapGadget($request);
	}
	public function executeHeatmapGadget(sfWebRequest $request){
		$this->setUserLocation($request);
	}
	public function executePublicHeatmapGadget(sfWebRequest $request){
		$this->setUserLocation($request);
	}
	public function executePublicMentalmapGadget(sfWebRequest $request){
		$this->setUserLocation($request);
	}
}
