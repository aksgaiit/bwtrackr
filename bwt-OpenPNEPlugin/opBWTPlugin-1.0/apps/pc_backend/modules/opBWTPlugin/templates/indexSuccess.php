<?php
/**
 * bwt configuration components.
 *
 * @package    OpenPNE
 * @subpackage bwt
 * @author     akosugi
 */
?>
<h2>Settings for bwTrackr plugin</h2>

<?php if (count($form) - 1): ?>
<form action="<?php echo url_for('opBWTPlugin/index') ?>" method="post">
<table>
<?php echo $form ?>
<tr>
<td colspan="2"><input type="submit" value="<?php echo __('Save') ?>" /></td>
</tr>
</table>
</form>
<?php else: ?>
<p>No item configurable..</p>
<?php endif; ?>
