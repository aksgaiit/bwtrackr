<?php
/**
 * diary components.
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */

class bwtActions extends sfActions{
	public function executeAuthProxy(opWebRequest $request){
		$this->member = $this->getUser()->getMember();
		return sfView::SUCCESS;
	}
}
