<?php
/**
 * diary components.
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */

class bwpubActions extends opJsonApiActions{
	protected function getPublicCollection(){
		return opBWTPluginUtil::getMapDatabase()->selectCollection("pub");
	}

	public function executeSearch(sfWebRequest $request){
		$criteria = array();
//todo getlocation from request and use geonear
		if(!is_null($criteria)){
			$col = $this->getPublicCollection();
			$items = $col->find($criteria);
			$data = array();
			foreach($items as $id=>$item){
				$data[] = $item;
			}
			return $this->renderJSON(array(
				"status"=>"success",
				"count"=>$items->count(),
				"items"=>$data,
			));
		}else{
			$this->forward400("invalid request parameter");
		}
	}
	public function executePost(sfWebRequest $request){
		$this->memberId = $this->getUser()->getMemberId();
		$formData = $request->getParameterHolder()->getAll();
		$sessionId = $formData["session"];
		$serialId = $formData["serial"];
		$num = $formData["num"];
		if(0 < $num){
			$entries = json_decode($formData["entries"]);
			foreach($entries as $entry){
				$entry->session = $sessionId;
				$entry->serial = $serialId;
				$col = $this->getPublicCollection();
				$col->update(array(
					"session" => $sessionId,
					"serial" => $serialId,
					"epoc"=> $entry->epoc,
				), $entry,
				array(
					"upsert" => true
				));
			}
		}
		return $this->renderJSON(array(
			"status"=>"success"
		));
	}

	protected function getMentalCollection(){
		return opBWTPluginUtil::getMapDatabase()->selectCollection("pubmental");
	}

	public function executeSearchMental(sfWebRequest $request){
		$criteria = array();
//todo getlocation from request and use geonear
		if(!is_null($criteria)){
			$col = $this->getMentalCollection();
			$items = $col->find($criteria);
			$data = array();
			foreach($items as $id=>$item){
				$data[] = $item;
			}
			return $this->renderJSON(array(
				"status"=>"success",
				"count"=>$items->count(),
				"items"=>$data,
			));
		}else{
			$this->forward400("invalid request parameter");
		}
	}
	public function executePostMental(sfWebRequest $request){
		$formData = $request->getParameterHolder()->getAll();
		$sessionId = $formData["session"];
		$serialId = $formData["serial"];
		$num = $formData["num"];
		if(0 < $num){
			$entries = json_decode($formData["entries"]);
			foreach($entries as $entry){
				$entry->session = $sessionId;
				$entry->serial = $serialId;
				$col = $this->getMentalCollection();
				$col->update(array(
					"session" => $sessionId,
					"serial" => $serialId,
					"epoc" => $entry->epoc,
				), $entry,
				array(
					"upsert" => true
				));
			}
		}
		return $this->renderJSON(array(
			"status" => "success"
		));
	}
}
