<?php
/**
 * bwt configuration components.
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */
class opBWTPluginActions extends sfActions{
  public function executeIndex(opWebRequest $request){
    $this->form = new opBWTPluginConfigurationForm();

    if ($request->isMethod(sfRequest::POST)){
      $this->form->bind($request->getParameter($this->form->getName()));
      if ($this->form->isValid()){
        $this->form->save();

        $this->getUser()->setFlash('notice', 'Saved configuration successfully.');

        $this->redirect('opBWTPlugin/index');
      }
    }
  }
}
