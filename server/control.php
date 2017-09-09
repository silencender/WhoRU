<?php

include('common.php');
require('dbManager.php');

$BASEDIR = './photos/';
$DIVIDE = '/';
$dbManager = dbManager::getInstance();
$method = (string)$_POST['method'];
$deviceID = (string)$_POST['deviceID'];
$value = (string)$_POST['value'];
/*$method = 'clear';
$deviceID = 'd41d8cd98f00b204e9800998ecf8427e';
$value = '{"name":"糊糊","picname":"aligned_1504970460223.jpg"}';*/

$info = array(
	'status' => 0,
	'message' => 'Not set'
	);

if(empty($method) || empty($deviceID) || empty($value)) {
	$info['message'] = 'Please check your request params';
	die(json_encode($info));
}

switch ($method) {
	case 'addNewDevice':
		$info['status'] = (int) ($dbManager->isDeviceIn($deviceID) || $dbManager->addNewDevice($deviceID));
		if($info['status']) {
			mkdir($BASEDIR. $deviceID);
			$info['message'] = "New device successfully added";
		} else {
			$info['message'] = "Failed to add new device";
		}
		break;

	case 'insert':
		$info['status'] = $dbManager->insert($deviceID,$value);
		if($info['status']) {
			mkdir($BASEDIR. $deviceID. $DIVIDE. $value);
			$info['message'] = "New person successfully added";
		} else {
			$info['message'] = "Failed to add new person";
		}
		break;

	case 'delete':
		$info['status'] = $dbManager->delete($deviceID,$value);
		if($info['status']) {
			rrmdir($BASEDIR. $deviceID. $DIVIDE. $value);
			$info['message'] = "Successfully deleted";
		} else {
			$info['message'] = "Failed to delete";
		}
		break;

	case 'clear':
		$info['status'] = $dbManager->clear($deviceID);
		if($info['status']) {
			rrmdir($BASEDIR. $deviceID);
			$info['message'] = "Successfully cleared";
		} else {
			$info['message'] = "Failed to clear";
		}
		break;

	case 'cleargal':
		$info['status'] = $dbManager->cleargal($deviceID,$value);
		if($info['status']) {
			$files = glob($BASEDIR. $deviceID. $DIVIDE. $value. $DIVIDE.'*');
			foreach($files as $file){
				if(is_file($file))
				unlink($file);
			}
			$info['message'] = "Successfully cleared";
		} else {
			$info['message'] = "Failed to clear";
		}
		break;

	case 'update':
		$person = json_decode($value);
		$info['status'] = $dbManager->remove_photo($person -> {'picname'}, $deviceID, $person -> {'name'});
		if($info['status']) {
			unlink($BASEDIR. $deviceID. $DIVIDE. $person -> {'name'}. $DIVIDE. $person -> {'picname'});
			$info['message'] = "Successfully removed";
		} else {
			$info['message'] = "Failed to remove";
		}
		break;

	default:
		break;
}

echo json_encode($info);