<?php
/**
 * bwtrackr api components.
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */

class bwdataActions extends opJsonApiActions{
	private $timeline;
	const DEFAULT_IMAGE_SIZE = 'large';

	public function preExecute(){
		parent::preExecute();
		$user = new opTimelineUser();
		$request = sfContext::getInstance()->getRequest();
		$params = array(
			"image_size" => $this->getRequestParameter("image_size", self::DEFAULT_IMAGE_SIZE),
			"base_url" => $request->getUriPrefix().$request->getRelativeUrlRoot(),
		);
		$this->timeline = new opTimeline($user, $params);
		$this->loadHelperForUseOpJsonAPI();
		$this->memberId = $this->getUser()->getMemberId();
	}
	private function loadHelperForUseOpJsonAPI(){
		$configuration = $this->getContext()->getConfiguration();
		$configuration->loadHelpers("opJsonApi");
		$configuration->loadHelpers("opUtil");
		$configuration->loadHelpers("Asset");
		$configuration->loadHelpers("Helper");
		$configuration->loadHelpers("Tag");
		$configuration->loadHelpers("sfImage");
	}
	protected function getSnapCollection(){
		return opBWTPluginUtil::getMapDatabase()->selectCollection("snaps");
	}

	public function searchBySession(sfWebRequest $request){
	}
	public function executeSearch(sfWebRequest $request){
		$sessionId = $request["session"];
		$scope = $request["scope"];
		$extract = $request["extrct"];
		$criteria = NULL;
		$this->memberId = $this->getUser()->getMemberId();
		if(!empty($sessionId)){
			$criteria = array(
				"session"=>$sessionId,
				"member"=>$this->memberId,
			);
		}else if("all" == $scope){
			$criteria = array();
		}else if("friend" == $scope){
			$ids = array();
			array_push($ids, $this->memberId);
			$member = $this->getUser()->getMember();
			foreach($member->getFriends() as $friend){
				$ids[] = $friend->getId();
			}
			$criteria = array(
				"member" => array(
					'$in' => $ids,
				),
			);
		}else if(empty($scope) || "my" == $scope){
			$criteria = array(
				"member"=>$this->memberId,
			);
		}

		if(!is_null($criteria)){
			$col = $this->getSnapCollection();
			$items = $col->find($criteria);
			$data = array();
			foreach($items as $id=>$item){
				if("true" == $extract){
					$member = Doctrine::getTable('Member')->find($item["member"]);
					$item["member"] = array(
						"id" => $member["id"],
						"name" => $member["name"],
					);
					if(!empty($item["ext"]) && "pne_activity" == $item["ext"]["type"]){
						$activity = Doctrine::getTable('ActivityData')->find($item["ext"]["id"]);
						$images = $activity->getImages();
						$imageurls = array();
						foreach($images as $image){
							$imageurls[] = sf_image_path($image->File, array(), true);
						}
						$item["ext"]["data"] = array(
							"body" => $activity["body"],
							"image_url" => $imageurls,
						);
					}
				}
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

	protected function assignFileInfo($thumbfile){
		$request = sfContext::getInstance()->getRequest();
		$ret = $thumbfile;
		$ret["stream"] = fopen($ret["tmp_name"], "r");
		$ret["dir_name"] = "/a".$this->memberId;
		$ret["binary"] = stream_get_contents($ret["stream"]);
		$ret["web_base_path"] = $request->getUriPrefix().$request->getRelativeUrlRoot();
		$ret["member_id"] = $this->memberId;
		return $ret;
	}
	protected function saveAsActivity($memberId, $data, $thumbfile){
		$options = array(
			"public_flag" => (isset($data["public_flag"]) ? $data["public_flag"] : 2),
			"source" => "API",
		);
		if(!empty($thumbfile)){
			$val = new opValidatorImageFile(array("required" => false));
			try{
				$obj = $val->clean($thumbfile);
				if(is_null($obj)){
					continue;
				}
error_log("thumbfile_ok");
				$file = new File();
				$file->setFromValidatedFile($obj);
				$file->setName("ac_".$this->getUser()->getMemberId()."_".$thumbfileobj["name"]);
				$file->save();
				$options['images'][]['file_id'] = $file->getId();
error_log("file id:".$file->getId());
			}catch(sfValidatorError $e){
				$this->forward400("Invalid image file.");
			}
		}
		$this->activity = Doctrine::getTable("ActivityData")->updateActivity($memberId, (empty($data["comment"]) ? "ｷﾀ━━━━━━(ﾟ∀ﾟ)━━━━━━ !!!!!" : $data["comment"]), $options);
	}
	public function executePost(sfWebRequest $request){
		$this->memberId = $this->getUser()->getMemberId();

		$formData = $request->getParameterHolder()->getAll();
		$sessionId = $formData["session"];
		if(!empty($this->memberId) && !empty($sessionId) && !empty($formData["epoc"])){
			$bwdata = array(
				"session"=>$sessionId,
				"member"=>$this->memberId,
				"epoc"=>$formData["epoc"],
				"loc"=>array(
					"lat"=>(double)$formData["lat"],
					"lng"=>(double)$formData["lng"],
				),
				"mental"=>array(
					"att"=>(double)$formData["att"],
					"med"=>(double)$formData["med"],
				),
				"wform"=>array(
					"len"=>(double)$formData["wlen"],
					"amp"=>(double)$formData["wamp"],
				),
				"comment"=>$formData["comment"],
			);
			$this->saveAsActivity($this->memberId, $bwdata, null);

			$thumbfile = $_FILES["bwdata-thumbnail-upload"];
			if(!empty($thumbfile) && 0 < (int)$thumbfile["size"]){
				$this->timeline->createActivityImageByFileInfoAndActivityId($this->assignFileInfo($thumbfile), $this->activity->getId());
//				$bwdata["thumb"] = new MongoBinData(file_get_contents($thumbfile["tmp_name"]));
			}
			$bwdata["ext"] = array(
				"type"=>"pne_activity",
				"id"=>$this->activity->getId()
			);
			$col = $this->getSnapCollection();
			$col->update(array(
				"session"=> $sessionId,
				"member"=> $this->memberId,
				"epoc"=>$formData["epoc"],
			), $bwdata,
			array(
				"upsert" => true
			));
			return $this->renderJSON(array(
				"status"=>"success"
			));
		}else{
			$this->forward400("insufficient request parameter");
		}
	}
}
